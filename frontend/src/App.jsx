import { useEffect, useState } from 'react';
import './App.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/tasks';

function App() {
  const [tasks, setTasks] = useState([]);
  const [title, setTitle] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editingTitle, setEditingTitle] = useState('');
  const [filter, setFilter] = useState('all');

  const loadTasks = async (selectedFilter = filter) => {
    let url = API_URL;

    if (selectedFilter === 'completed') {
      url += '?completed=true';
    }

    if (selectedFilter === 'pending') {
      url += '?completed=false';
    }

    const response = await fetch(url);
    const data = await response.json();
    setTasks(data);
  };

  const changeFilter = (selectedFilter) => {
    setFilter(selectedFilter);
    loadTasks(selectedFilter);
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
    </main>
  );
}

export default App;