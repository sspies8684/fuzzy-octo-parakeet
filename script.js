const STORAGE_KEY = "focusflow.tasks";

const taskForm = document.querySelector("[data-task-form]");
const taskInput = document.querySelector("[data-task-input]");
const taskList = document.querySelector("[data-task-list]");
const filterButtons = Array.from(document.querySelectorAll("[data-filter]"));
const emptyState = document.querySelector("[data-empty-state]");

let tasks = [];
let currentFilter = "all";

const uuid = () =>
  (typeof crypto !== "undefined" && crypto.randomUUID
    ? crypto.randomUUID()
    : `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`);

const formatCreatedAt = (timestamp) =>
  new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(timestamp));

const loadTasks = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    tasks = stored ? JSON.parse(stored) : [];
  } catch (error) {
    console.error("Failed to load tasks from localStorage", error);
    tasks = [];
  }
};

const saveTasks = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
  } catch (error) {
    console.error("Failed to save tasks to localStorage", error);
  }
};

const setFilter = (filter) => {
  currentFilter = filter;
  filterButtons.forEach((button) => {
    button.classList.toggle("is-active", button.dataset.filter === filter);
  });
  renderTasks();
};

const addTask = (title) => {
  const task = {
    id: uuid(),
    title,
    completed: false,
    createdAt: Date.now(),
  };
  tasks = [task, ...tasks];
  saveTasks();
  renderTasks();
};

const toggleTask = (taskId) => {
  tasks = tasks.map((task) =>
    task.id === taskId ? { ...task, completed: !task.completed } : task
  );
  saveTasks();
  renderTasks();
};

const deleteTask = (taskId) => {
  tasks = tasks.filter((task) => task.id !== taskId);
  saveTasks();
  renderTasks();
};

const getFilteredTasks = () => {
  switch (currentFilter) {
    case "active":
      return tasks.filter((task) => !task.completed);
    case "completed":
      return tasks.filter((task) => task.completed);
    default:
      return tasks;
  }
};

const createTaskElement = (task) => {
  const item = document.createElement("li");
  item.className = "task";
  if (task.completed) {
    item.classList.add("completed");
  }
  item.dataset.id = task.id;

  const checkbox = document.createElement("input");
  checkbox.type = "checkbox";
  checkbox.className = "task__checkbox";
  checkbox.checked = task.completed;
  checkbox.setAttribute("aria-label", `Mark "${task.title}" as complete`);
  checkbox.addEventListener("change", () => toggleTask(task.id));

  const label = document.createElement("div");
  label.className = "task__label";

  const title = document.createElement("p");
  title.className = "task__title";
  title.textContent = task.title;

  const meta = document.createElement("span");
  meta.className = "task__meta";
  meta.textContent = `Added ${formatCreatedAt(task.createdAt)}`;

  label.append(title, meta);

  const actions = document.createElement("div");
  actions.className = "task__actions";

  const deleteButton = document.createElement("button");
  deleteButton.className = "task__delete";
  deleteButton.type = "button";
  deleteButton.textContent = "Delete";
  deleteButton.addEventListener("click", () => deleteTask(task.id));

  actions.append(deleteButton);

  item.append(checkbox, label, actions);
  return item;
};

const renderTasks = () => {
  const filtered = getFilteredTasks();
  taskList.replaceChildren(...filtered.map(createTaskElement));

  const hasTasks = filtered.length > 0;
  emptyState.hidden = hasTasks;
  if (!hasTasks) {
    const totalTasks = tasks.length;
    emptyState.textContent =
      totalTasks === 0
        ? "Add your first task to get going!"
        : `No ${currentFilter} tasks right now. Nice work!`;
  }
};

taskForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const title = taskInput.value.trim();
  if (!title) return;

  addTask(title);
  taskForm.reset();
  taskInput.focus();
});

filterButtons.forEach((button) => {
  button.addEventListener("click", () => {
    setFilter(button.dataset.filter);
  });
});

window.addEventListener("DOMContentLoaded", () => {
  loadTasks();
  renderTasks();
  taskInput.focus();
});
