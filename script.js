const STORAGE_KEY = "focusflow.tasks";

const taskForm = document.querySelector("[data-task-form]");
const taskInput = document.querySelector("[data-task-input]");
const taskList = document.querySelector("[data-task-list]");
const filterButtons = Array.from(document.querySelectorAll("[data-filter]"));
const emptyState = document.querySelector("[data-empty-state]");
const surpriseButton = document.querySelector("[data-surprise-button]");
const surprisePanel = document.querySelector("[data-surprise-panel]");
const surpriseMessage = document.querySelector("[data-surprise-message]");
const surpriseClose = document.querySelector("[data-surprise-close]");

let tasks = [];
let currentFilter = "all";
let partyModeIntervalId = null;
const confettiTimeouts = new Set();

const surpriseMessages = [
  "Focus Goblin says: Schedule your chaos like it’s an important meeting.",
  "Achievement unlocked: Procrastination justified by 'creative ideation break.'",
  "Reminder: Hydrate! (Focus Goblin counts coffee, tea, and dramatic sighs.)",
  "Plot twist: That task was already done in a parallel universe. Party anyway!",
  "Focus Goblin suggests: Rename every task to 'dance break' and see what happens.",
];

const confettiEmojis = ["🦄", "🧠", "🪩", "🍕", "🤖", "🦸‍♀️", "🥳", "🛸"];

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

const randomFrom = (items) => items[Math.floor(Math.random() * items.length)];

const launchConfetti = (count = 14) => {
  for (let index = 0; index < count; index += 1) {
    const confetti = document.createElement("span");
    confetti.className = "confetti";
    confetti.textContent = randomFrom(confettiEmojis);
    confetti.style.left = `${Math.random() * 100}vw`;
    confetti.style.setProperty("--duration", `${3 + Math.random() * 2}s`);
    document.body.append(confetti);
    const timeoutId = setTimeout(() => {
      confetti.remove();
      confettiTimeouts.delete(timeoutId);
    }, 5000);
    confettiTimeouts.add(timeoutId);
  }
};

const enterPartyMode = () => {
  if (document.body.classList.contains("party-mode") || !surpriseButton) return;
  document.body.classList.add("party-mode");
  surpriseButton.textContent = "Undo the chaos";
  surpriseButton.setAttribute("aria-expanded", "true");
  if (surprisePanel) {
    surprisePanel.hidden = false;
  }
  if (surpriseMessage) {
    surpriseMessage.textContent = randomFrom(surpriseMessages);
  }
  launchConfetti();
  partyModeIntervalId = setInterval(() => launchConfetti(10), 4000);
};

const exitPartyMode = () => {
  if (!document.body.classList.contains("party-mode") || !surpriseButton) return;
  document.body.classList.remove("party-mode");
  surpriseButton.textContent = "Do Not Press";
  surpriseButton.setAttribute("aria-expanded", "false");
  if (surprisePanel) {
    surprisePanel.hidden = true;
  }
  if (partyModeIntervalId) {
    clearInterval(partyModeIntervalId);
    partyModeIntervalId = null;
  }
  confettiTimeouts.forEach((timeoutId) => {
    clearTimeout(timeoutId);
  });
  confettiTimeouts.clear();
  document.querySelectorAll(".confetti").forEach((node) => node.remove());
};

const togglePartyMode = () => {
  if (document.body.classList.contains("party-mode")) {
    exitPartyMode();
  } else {
    enterPartyMode();
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

surpriseButton?.addEventListener("click", togglePartyMode);
surpriseClose?.addEventListener("click", exitPartyMode);
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    exitPartyMode();
  }
});
