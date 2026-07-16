import { useEffect, useState } from 'react';
import './App.css';
import { apiFetch } from './api';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/tasks';
const PAGE_API_URL = `${API_URL}/page`;

function App({ keycloak }) {
  const [tasks, setTasks] = useState([]);
  const [title, setTitle] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editingTitle, setEditingTitle] = useState('');
  const [filter, setFilter] = useState('all');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [last, setLast] = useState(true);
  const pageSize = 2;
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [titleError, setTitleError] = useState('');

  const loadTasks = async (
    selectedFilter = filter,
    selectedSearch = search,
    selectedPage = page
  ) => {
    try {
      setLoading(true);
      setError('');

      const params = new URLSearchParams();

      params.append('page', selectedPage);
      params.append('size', pageSize);

      if (selectedFilter === 'completed') {
        params.append('completed', 'true');
      }

      if (selectedFilter === 'pending') {
        params.append('completed', 'false');
      }

      if (selectedSearch.trim()) {
        params.append('search', selectedSearch.trim());
      }

      const response = await apiFetch(`${PAGE_API_URL}?${params.toString()}`);

      if (!response.ok) {
        throw new Error('Error loading tasks');
      }

      const data = await response.json();

      setTasks(data.content);
      setPage(data.page);
      setTotalPages(data.totalPages);
      setLast(data.last);
    } catch (error) {
      setError(error.message);
    } finally {
      setLoading(false);
    }
  };

  const changeFilter = (selectedFilter) => {
    setFilter(selectedFilter);
    setPage(0);
    loadTasks(selectedFilter, search, 0);
  };

  const searchTasks = (e) => {
    e.preventDefault();
    setPage(0);
    loadTasks(filter, search, 0);
  };

  const clearSearch = () => {
    setSearch('');
    setPage(0);
    loadTasks(filter, '', 0);
  };

  const previousPage = () => {
    if (page === 0) return;
    loadTasks(filter, search, page - 1);
  };

  const nextPage = () => {
    if (last) return;
    loadTasks(filter, search, page + 1);
  };

  const createTask = async (e) => {
    e.preventDefault();

    if (!title.trim()) {
      setTitleError('Task title is required');
      return;
    }

    setTitleError('');

    await apiFetch(API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title,
        description: 'Created from React frontend',
        completed: false
      })
    });

    setTitle('');
    loadTasks(filter, search, 0);
  };

  const toggleTask = async (task) => {
    await apiFetch(`${API_URL}/${task.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: task.title,
        description: task.description,
        completed: !task.completed
      })
    });

    loadTasks();
  };

  const startEditing = (task) => {
    setEditingId(task.id);
    setEditingTitle(task.title);
  };

  const cancelEditing = () => {
    setEditingId(null);
    setEditingTitle('');
  };

  const saveEditing = async (task) => {
    if (!editingTitle.trim()) return;

    await apiFetch(`${API_URL}/${task.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title: editingTitle,
        description: task.description,
        completed: task.completed
      })
    });

    cancelEditing();
    loadTasks();
  };

  const deleteTask = async (id) => {
    const confirmed = window.confirm('Are you sure you want to delete this task?');

    if (!confirmed) return;

    await apiFetch(`${API_URL}/${id}`, {
      method: 'DELETE'
    });

    loadTasks(filter, search, page);
  };

  useEffect(() => {
    loadTasks();
  }, []);

  return (
    <main>
      <h1>Task Manager</h1>

      <div className="session-bar">
        <span>
          Signed in as: {keycloak.tokenParsed?.preferred_username}
        </span>

        <button
          type="button"
          className="logout-button"
          onClick={() => keycloak.logout({ redirectUri: window.location.origin })}
        >
          Logout
        </button>
      </div>

      <form onSubmit={createTask}>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="New task title"
        />
        <button type="submit">Add</button>
      </form>

      {titleError && <p className="error">{titleError}</p>}

      <form className="search-form" onSubmit={searchTasks}>
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search task..."
        />
        <button type="submit">Search</button>
        <button type="button" onClick={clearSearch}>Clear</button>
      </form>

      <div className="filters">
        <button
          className={filter === 'all' ? 'active' : ''}
          onClick={() => changeFilter('all')}
        >
          All
        </button>

        <button
          className={filter === 'pending' ? 'active' : ''}
          onClick={() => changeFilter('pending')}
        >
          Pending
        </button>

        <button
          className={filter === 'completed' ? 'active' : ''}
          onClick={() => changeFilter('completed')}
        >
          Completed
        </button>
      </div>

      {loading && <p className="loading">Loading tasks...</p>}

      {error && <p className="error">{error}</p>}

      <ul>
        {tasks.map((task) => (
          <li key={task.id}>
            <input
              type="checkbox"
              checked={task.completed}
              onChange={() => toggleTask(task)}
            />

            {editingId === task.id ? (
              <>
                <input
                  value={editingTitle}
                  onChange={(e) => setEditingTitle(e.target.value)}
                />

                <button type="button" onClick={() => saveEditing(task)}>
                  Save
                </button>

                <button type="button" onClick={cancelEditing}>
                  Cancel
                </button>
              </>
            ) : (
              <>
                <div className="task-info">
                  <span className={task.completed ? 'task-title completed' : 'task-title'}>
                    {task.title}
                  </span>

                  <small>
                    Created: {new Date(task.createdAt).toLocaleString()}
                  </small>
                </div>

                <button type="button" onClick={() => startEditing(task)}>
                  Edit
                </button>

                <button type="button" onClick={() => deleteTask(task.id)}>
                  Delete
                </button>
              </>
            )}

          </li>
        ))}

        {!loading && !error && tasks.length === 0 && (
          <p className="empty">No tasks found.</p>
        )}

      </ul>

      <div className="pagination">
        <button onClick={previousPage} disabled={page === 0}>
          Previous
        </button>

        <span>
          Page {page + 1} of {totalPages || 1}
        </span>

        <button onClick={nextPage} disabled={last}>
          Next
        </button>
      </div>
    </main>
  );
}

export default App;