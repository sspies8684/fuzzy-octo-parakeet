const STORAGE_KEY = "focusflow.tasks";

const root = document.documentElement;
const taskForm = document.querySelector("[data-task-form]");
const taskInput = document.querySelector("[data-task-input]");
const taskList = document.querySelector("[data-task-list]");
const filterButtons = Array.from(document.querySelectorAll("[data-filter]"));
const emptyState = document.querySelector("[data-empty-state]");
const surpriseButton = document.querySelector("[data-surprise-button]");
const surprisePanel = document.querySelector("[data-surprise-panel]");
const surpriseMessage = document.querySelector("[data-surprise-message]");
const surpriseClose = document.querySelector("[data-surprise-close]");
const taskSubmitButton = document.querySelector(".task-form__submit");
const backgroundOrbs = Array.from(document.querySelectorAll("[data-orb]"));
const backgroundGradient = document.querySelector("[data-gradient]");
const motionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");

let tasks = [];
let currentFilter = "all";
let partyModeIntervalId = null;
const confettiTimeouts = new Set();

const pointerPosition = { x: 0.5, y: 0.5 };
let orbAnimationFrameId = null;
let orbAnimationStart = null;
let surprisePanelTransitionHandler = null;

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
    const parsed = stored ? JSON.parse(stored) : [];
    tasks = parsed.map((task) => ({
      ...task,
      createdAt:
        typeof task.createdAt === "number" ? task.createdAt : Date.now(),
      updatedAt:
        typeof task.updatedAt === "number"
          ? task.updatedAt
          : typeof task.createdAt === "number"
          ? task.createdAt
          : Date.now(),
    }));
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
  const timestamp = Date.now();
  const task = {
    id: uuid(),
    title,
    completed: false,
    createdAt: timestamp,
    updatedAt: timestamp,
  };
  tasks = [task, ...tasks];
  saveTasks();
  renderTasks();
};

const toggleTask = (taskId) => {
  const timestamp = Date.now();
  tasks = tasks.map((task) =>
    task.id === taskId
      ? { ...task, completed: !task.completed, updatedAt: timestamp }
      : task
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

const registerTiltMotion = (element, intensity = 8) => {
  if (!element || element.dataset.tiltAttached === "true") return;
  element.style.setProperty("--tilt-x", "0deg");
  element.style.setProperty("--tilt-y", "0deg");

  const updateTilt = (event) => {
    if (motionQuery.matches) return;
    const rect = element.getBoundingClientRect();
    if (!rect.width || !rect.height) return;
    const relativeX = (event.clientX - rect.left) / rect.width - 0.5;
    const relativeY = (event.clientY - rect.top) / rect.height - 0.5;
    element.style.setProperty(
      "--tilt-x",
      `${(-relativeY * intensity).toFixed(3)}deg`
    );
    element.style.setProperty(
      "--tilt-y",
      `${(relativeX * intensity).toFixed(3)}deg`
    );
  };

  const resetTilt = () => {
    element.style.setProperty("--tilt-x", "0deg");
    element.style.setProperty("--tilt-y", "0deg");
  };

  element.addEventListener("pointermove", updateTilt);
  element.addEventListener("pointerleave", resetTilt);
  element.addEventListener("pointerup", resetTilt);
  element.dataset.tiltAttached = "true";
};

const createTaskElement = (task, index) => {
  const item = document.createElement("li");
  item.className = "task";
  item.dataset.id = task.id;
  item.style.setProperty("--item-index", index ?? 0);

  if (task.completed) {
    item.classList.add("completed");
  }

  const now = Date.now();
  let motionState = null;

  if (now - task.createdAt < 600) {
    motionState = "new";
  } else if (
    task.completed &&
    typeof task.updatedAt === "number" &&
    now - task.updatedAt < 600
  ) {
    motionState = "completed";
  }

  if (motionState) {
    item.dataset.motion = motionState;
    item.addEventListener(
      "animationend",
      (event) => {
        if (
          (motionState === "new" && event.animationName === "task-enter") ||
          (motionState === "completed" && event.animationName === "task-complete")
        ) {
          item.removeAttribute("data-motion");
        }
      },
      { once: true }
    );
  }

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
  registerTiltMotion(item, 10);
  return item;
};

const renderTasks = () => {
  const filtered = getFilteredTasks();
  const fragment = document.createDocumentFragment();
  filtered.forEach((task, index) => {
    fragment.append(createTaskElement(task, index));
  });
  taskList.replaceChildren(fragment);

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
  if (motionQuery.matches) return;
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

const updatePointerVariables = () => {
  root.style.setProperty("--pointer-x", pointerPosition.x.toFixed(4));
  root.style.setProperty("--pointer-y", pointerPosition.y.toFixed(4));
};

const handlePointerMove = (event) => {
  pointerPosition.x = event.clientX / window.innerWidth;
  pointerPosition.y = event.clientY / window.innerHeight;
  updatePointerVariables();
};

const animateBackground = (timestamp) => {
  if (motionQuery.matches) {
    orbAnimationFrameId = null;
    return;
  }
  if (!orbAnimationStart) {
    orbAnimationStart = timestamp;
  }
  const elapsed = (timestamp - orbAnimationStart) / 1000;

  backgroundOrbs.forEach((orb, index) => {
    const depth = (index + 1) / backgroundOrbs.length;
    const offsetX = (pointerPosition.x - 0.5) * (36 + depth * 40);
    const offsetY = (pointerPosition.y - 0.5) * (28 + depth * 36);
    const float = Math.sin(elapsed * (0.8 + depth * 0.22)) * (14 + depth * 18);
    orb.style.setProperty(
      "--orb-transform",
      `translate3d(${offsetX.toFixed(2)}px, ${(offsetY + float).toFixed(
        2
      )}px, 0)`
    );
  });

  if (backgroundGradient) {
    const hue = 18 + Math.sin(elapsed * 0.15) * 36;
    backgroundGradient.style.setProperty("--gradient-hue", hue.toFixed(2));
  }

  orbAnimationFrameId = requestAnimationFrame(animateBackground);
};

const startBackgroundMotion = () => {
  if (!backgroundOrbs.length || motionQuery.matches) return;
  stopBackgroundMotion();
  pointerPosition.x = 0.5;
  pointerPosition.y = 0.5;
  updatePointerVariables();
  orbAnimationStart = null;
  window.addEventListener("pointermove", handlePointerMove, { passive: true });
  orbAnimationFrameId = requestAnimationFrame(animateBackground);
};

const stopBackgroundMotion = () => {
  window.removeEventListener("pointermove", handlePointerMove);
  if (orbAnimationFrameId) {
    cancelAnimationFrame(orbAnimationFrameId);
    orbAnimationFrameId = null;
  }
  backgroundOrbs.forEach((orb) => {
    orb.style.removeProperty("--orb-transform");
  });
  backgroundGradient?.style.removeProperty("--gradient-hue");
};

const showSurprisePanel = () => {
  if (!surprisePanel) return;
  if (surprisePanelTransitionHandler) {
    surprisePanel.removeEventListener(
      "transitionend",
      surprisePanelTransitionHandler
    );
    surprisePanelTransitionHandler = null;
  }
  surprisePanel.hidden = false;
  requestAnimationFrame(() => {
    surprisePanel.classList.add("is-visible");
  });
};

const hideSurprisePanel = () => {
  if (!surprisePanel) return;
  if (surprisePanelTransitionHandler) {
    surprisePanel.removeEventListener(
      "transitionend",
      surprisePanelTransitionHandler
    );
    surprisePanelTransitionHandler = null;
  }
  surprisePanel.classList.remove("is-visible");

  if (motionQuery.matches) {
    surprisePanel.hidden = true;
    return;
  }

  surprisePanelTransitionHandler = () => {
    surprisePanel.hidden = true;
    surprisePanel.removeEventListener(
      "transitionend",
      surprisePanelTransitionHandler
    );
    surprisePanelTransitionHandler = null;
  };

  surprisePanel.addEventListener(
    "transitionend",
    surprisePanelTransitionHandler,
    { once: true }
  );
};

const enterPartyMode = () => {
  if (document.body.classList.contains("party-mode") || !surpriseButton) return;
  document.body.classList.add("party-mode");
  surpriseButton.textContent = "Undo the chaos";
  surpriseButton.setAttribute("aria-expanded", "true");
  if (surpriseMessage) {
    surpriseMessage.textContent = randomFrom(surpriseMessages);
  }
  showSurprisePanel();
  launchConfetti();
  if (!motionQuery.matches) {
    partyModeIntervalId = setInterval(() => launchConfetti(10), 4000);
  }
};

const exitPartyMode = () => {
  if (!document.body.classList.contains("party-mode") || !surpriseButton) return;
  document.body.classList.remove("party-mode");
  surpriseButton.textContent = "Do Not Press";
  surpriseButton.setAttribute("aria-expanded", "false");
  hideSurprisePanel();
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

const handleMotionPreferenceChange = (event) => {
  if (event.matches) {
    stopBackgroundMotion();
  } else {
    startBackgroundMotion();
  }
};

if (typeof motionQuery.addEventListener === "function") {
  motionQuery.addEventListener("change", handleMotionPreferenceChange);
} else if (typeof motionQuery.addListener === "function") {
  motionQuery.addListener(handleMotionPreferenceChange);
}

taskForm?.addEventListener("submit", (event) => {
  event.preventDefault();
  const title = taskInput.value.trim();
  if (!title) return;

  addTask(title);
  taskForm.reset();
  taskInput?.focus();
});

filterButtons.forEach((button) => {
  button.addEventListener("click", () => {
    setFilter(button.dataset.filter);
  });
  registerTiltMotion(button, 6);
});

window.addEventListener("DOMContentLoaded", () => {
  loadTasks();
  renderTasks();
  taskInput?.focus();
  startBackgroundMotion();
  document.body.classList.add("is-ready");

  registerTiltMotion(taskSubmitButton, 8);
  registerTiltMotion(surpriseButton, 10);
});

surpriseButton?.addEventListener("click", togglePartyMode);
surpriseClose?.addEventListener("click", exitPartyMode);
document.addEventListener("keydown", (event) => {
  if (event.key === "Escape") {
    exitPartyMode();
  }
});
