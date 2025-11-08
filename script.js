const STORAGE_KEY = "focusflow.tasks";

const taskForm = document.querySelector("[data-task-form]");
const taskInput = document.querySelector("[data-task-input]");
const taskList = document.querySelector("[data-task-list]");
const filterButtons = Array.from(document.querySelectorAll("[data-filter]"));
const emptyState = document.querySelector("[data-empty-state]");
const tiltElements = Array.from(document.querySelectorAll("[data-tilt]"));
const prefersReducedMotion =
  typeof window.matchMedia === "function" &&
  window.matchMedia("(prefers-reduced-motion: reduce)").matches;

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
  const finalizeDelete = () => {
    tasks = tasks.filter((task) => task.id !== taskId);
    saveTasks();
    renderTasks();
  };

  const item = taskList.querySelector(`[data-id="${taskId}"]`);

  if (!item || prefersReducedMotion || typeof item.animate !== "function") {
    finalizeDelete();
    return;
  }

  if (item.dataset.deleting === "true") return;
  item.dataset.deleting = "true";

  const animation = item.animate(
    [
      { opacity: 1, transform: "translateY(0) scale(1)" },
      { opacity: 0, transform: "translateY(12px) scale(0.96)" },
    ],
    {
      duration: 260,
      easing: "cubic-bezier(0.4, 0, 0.2, 1)",
      fill: "forwards",
    }
  );

  const finish = () => finalizeDelete();
  animation.addEventListener("finish", finish, { once: true });
  animation.addEventListener("cancel", finish, { once: true });
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

const animateTaskEntry = (element, index = 0) => {
  if (prefersReducedMotion || typeof element.animate !== "function") return;
  requestAnimationFrame(() => {
    if (!element.isConnected) return;
    const delay = Math.min(index * 40, 280);
    element.animate(
      [
        { opacity: 0, transform: "translateY(16px) scale(0.98)" },
        { opacity: 1, transform: "translateY(0) scale(1)" },
      ],
      {
        duration: 420,
        easing: "cubic-bezier(0.22, 1, 0.36, 1)",
        delay,
        fill: "both",
      }
    );
  });
};

const createTaskElement = (task, index = 0) => {
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
  animateTaskEntry(item, index);
  return item;
};

const renderTasks = () => {
  const filtered = getFilteredTasks();
  taskList.replaceChildren(
    ...filtered.map((task, index) => createTaskElement(task, index))
  );

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

tiltElements.forEach((element) => {
  const maxTilt = 6;
  let rafId = 0;

  const resetTilt = () => {
    element.style.setProperty("--tilt-x", "0deg");
    element.style.setProperty("--tilt-y", "0deg");
  };

  const onPointerMove = (event) => {
    if (prefersReducedMotion) return;
    if (rafId) cancelAnimationFrame(rafId);

    rafId = requestAnimationFrame(() => {
      const rect = element.getBoundingClientRect();
      const offsetX = event.clientX - rect.left;
      const offsetY = event.clientY - rect.top;
      const centerX = rect.width / 2;
      const centerY = rect.height / 2;
      const rotateX = ((offsetY - centerY) / centerY) * -maxTilt;
      const rotateY = ((offsetX - centerX) / centerX) * maxTilt;
      element.style.setProperty("--tilt-x", `${rotateX.toFixed(2)}deg`);
      element.style.setProperty("--tilt-y", `${rotateY.toFixed(2)}deg`);
    });
  };

  element.addEventListener("pointermove", onPointerMove);
  element.addEventListener("pointerleave", () => {
    if (rafId) cancelAnimationFrame(rafId);
    resetTilt();
  });
  element.addEventListener("touchend", resetTilt);
  element.addEventListener("pointercancel", resetTilt);
});

window.addEventListener("DOMContentLoaded", () => {
  loadTasks();
  renderTasks();
  taskInput.focus();
});
