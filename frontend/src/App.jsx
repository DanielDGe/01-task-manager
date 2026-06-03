import { useEffect, useState } from 'react';
import './App.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/tasks';
const PAGE_API_URL = `${API_URL}/page`;

function App() {
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

  const loadTasks = async (
    selectedFilter = filter,
    selectedSearch = search,
    selectedPage = page
  ) => {
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

    const response = await fetch(`${PAGE_API_URL}?${params.toString()}`);
    const data = await response.json();

    setTasks(data.content);
    setPage(data.page);
    setTotalPages(data.totalPages);
    setLast(data.last);
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

    if (!title.trim()) return;

    await fetch(API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        title,
        description: 'Created from React frontend',
        completed: false
      })
    });

    setTitle('');
    loadTasks();
  };

  const toggleTask = async (task) => {
    await fetch(`${API_URL}/${task.id}`, {
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

    await fetch(`${API_URL}/${task.id}`, {
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
    await fetch(`${API_URL}/${id}`, {
      method: 'DELETE'
    });

    loadTasks();
  };

  useEffect(() => {
    loadTasks();
  }, []);

  return (
    <main>
      <h1>Task Manager</h1>

      <form onSubmit={createTask}>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="New task title"
        />
        <button type="submit">Add</button>
      </form>

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
        <button onClick={() => changeFilter('all')}>All</button>
        <button onClick={() => changeFilter('pending')}>Pending</button>
        <button onClick={() => changeFilter('completed')}>Completed</button>
      </div>

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
                <button onClick={() => saveEditing(task)}>Save</button>
                <button onClick={cancelEditing}>Cancel</button>
              </>
            ) : (
              <>
                <span style={{ textDecoration: task.completed ? 'line-through' : 'none' }}>
                  {task.title}
                </span>
                <button onClick={() => startEditing(task)}>Edit</button>
                <button onClick={() => deleteTask(task.id)}>Delete</button>
              </>
            )}
          </li>
        ))}
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