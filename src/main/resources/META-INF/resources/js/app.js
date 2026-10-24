const API = {
  resources: "/api/v1/rental-item/all",
  reservations: "/api/v1/reservation/all",
  health: "/q/health/ready",
  createReservation: "/api/v1/reservation",
  reservationStatus: (id, status) => `/api/v1/reservation/${encodeURIComponent(id)}/${encodeURIComponent(status)}`
};

const state = { resources: [], reservations: [], filter: "ALL", search: "" };
const elements = {
  resourceGrid: document.querySelector("#resource-grid"),
  reservationList: document.querySelector("#reservation-list"),
  resourceError: document.querySelector("#resource-error"),
  reservationError: document.querySelector("#reservation-error"),
  resourceSearch: document.querySelector("#resource-search"),
  bookingDialog: document.querySelector("#booking-dialog"),
  bookingForm: document.querySelector("#booking-form"),
  bookingResourceId: document.querySelector("#booking-resource-id"),
  selectedResource: document.querySelector("#selected-resource"),
  startsAt: document.querySelector("#starts-at"),
  endsAt: document.querySelector("#ends-at"),
  reservedBy: document.querySelector("#reserved-by"),
  formMessage: document.querySelector("#form-message"),
  submitBooking: document.querySelector("#submit-booking"),
  toastRegion: document.querySelector("#toast-region"),
  healthApplication: document.querySelector("#health-application"),
  healthDatabase: document.querySelector("#health-database")
};

const icons = {
  ROOM: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 20V8.5L12 4l8 4.5V20M8 20v-6h8v6M4 10l8 4 8-4"/></svg>',
  EQUIPMENT: '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="7" width="16" height="12" rx="2"/><path d="M9 7V5h6v2M8 12h8m-6 3h4"/></svg>',
  VEHICLE: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m3 15 1.5-5a2 2 0 0 1 1.9-1.5h11.2a2 2 0 0 1 1.9 1.5L21 15v4h-2v-2H5v2H3v-4Z"/><path d="M5 14h14M7 11h.01M17 11h.01"/></svg>'
};
const typeNames = { ROOM: "Space", EQUIPMENT: "Equipment", VEHICLE: "Vehicle" };

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);
}

function localDateTimeValue(date) {
  const pad = value => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function displayDate(value) {
  if (!value) return "Date not set";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, { month: "short", day: "numeric", year: "numeric" }).format(date);
}

function displayTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit" }).format(date);
}

function resourceType(type) {
  return typeNames[type] || "Resource";
}

async function fetchJson(url, options) {
  const response = await fetch(url, {
    ...options,
    headers: { Accept: "application/json", ...(options?.body ? { "Content-Type": "application/json" } : {}), ...options?.headers }
  });
  if (!response.ok) {
    const detail = await response.text();
    let message = detail;
    try {
      const parsed = JSON.parse(detail);
      message = parsed.message || parsed.error || detail;
    } catch {
      // Keep the response text when the API does not return JSON.
    }
    throw new Error(message || `Request failed (${response.status})`);
  }
  if (response.status === 204) return null;
  return response.json();
}

function setError(element, message) {
  element.textContent = message;
  element.hidden = !message;
}

function showToast(message, isError = false) {
  const toast = document.createElement("div");
  toast.className = `toast${isError ? " error" : ""}`;
  toast.textContent = message;
  elements.toastRegion.append(toast);
  window.setTimeout(() => toast.remove(), 4000);
}

function renderStats() {
  document.querySelector("#stat-total").textContent = String(state.resources.length);
  document.querySelector("#stat-available").textContent =
    String(state.resources.filter(resource => resource.status === "AVAILABLE").length);
  document.querySelector("#stat-reservations").textContent = String(state.reservations.length);
  document.querySelector("#filter-all-count").textContent = String(state.resources.length);
}

function visibleResources() {
  const query = state.search.trim().toLocaleLowerCase();
  return state.resources.filter(resource => {
    const matchesFilter = state.filter === "ALL" || resource.type === state.filter;
    const matchesSearch = !query || `${resource.name} ${resource.type}`.toLocaleLowerCase().includes(query);
    return matchesFilter && matchesSearch;
  });
}

function renderResources() {
  const resources = visibleResources();
  if (!resources.length) {
    elements.resourceGrid.innerHTML = `<div class="empty-state"><strong>${state.resources.length ? "Nothing matches that search" : "No resources yet"}</strong>${state.resources.length ? "Try another name or category." : "There are no bookable resources in this workspace yet."}</div>`;
    return;
  }
  elements.resourceGrid.innerHTML = resources.map(resource => {
    const available = resource.status === "AVAILABLE";
    const type = escapeHtml(resource.type);
    return `<article class="resource-card">
      <div class="resource-card-top">
        <span class="resource-illustration ${type.toLowerCase()}">${icons[resource.type] || icons.EQUIPMENT}</span>
        <span class="availability${available ? "" : " is-unavailable"}">${available ? "Available" : "Unavailable"}</span>
      </div>
      <h3 title="${escapeHtml(resource.name)}">${escapeHtml(resource.name)}</h3>
      <div class="resource-meta"><span>${escapeHtml(resourceType(resource.type))}</span><span class="dot">•</span><span>Shared resource</span></div>
      <div class="resource-card-bottom">
        <small>Ready for your next plan</small>
        <button class="button ${available ? "button-primary" : "button-secondary"}" type="button" data-book-resource="${escapeHtml(resource.id)}" ${available ? "" : "disabled"}>${available ? "Book now" : "Not available"}${available ? ' <span aria-hidden="true">→</span>' : ""}</button>
      </div>
    </article>`;
  }).join("");
}

function reservationResourceName(reservation) {
  const resource = state.resources.find(item => String(item.id) === String(reservation.rentalItemId));
  return resource?.name || `Resource #${reservation.rentalItemId}`;
}

function statusLabel(status) {
  if (!status) return "Unknown";
  return status.charAt(0) + status.slice(1).toLowerCase();
}

function renderReservations() {
  if (!state.reservations.length) {
    elements.reservationList.innerHTML = '<tr><td class="table-empty" colspan="5">No reservations yet. Your bookings will show up here.</td></tr>';
    return;
  }
  const sorted = [...state.reservations].sort((a, b) => new Date(b.startsAt) - new Date(a.startsAt));
  elements.reservationList.innerHTML = sorted.map(reservation => {
    const status = String(reservation.status || "").toLowerCase();
    const canCancel = ["PENDING", "CONFIRMED"].includes(reservation.status);
    const resource = state.resources.find(item => String(item.id) === String(reservation.rentalItemId));
    const icon = icons[resource?.type] || icons.EQUIPMENT;
    return `<tr>
      <td><span class="table-resource"><span class="table-resource-icon">${icon}</span><span>${escapeHtml(reservationResourceName(reservation))}<small class="table-secondary">${escapeHtml(resourceType(resource?.type))}</small></span></span></td>
      <td>${escapeHtml(reservation.reservedBy)}</td>
      <td>${escapeHtml(displayDate(reservation.startsAt))}<small class="table-secondary">${escapeHtml(displayTime(reservation.startsAt))} – ${escapeHtml(displayTime(reservation.endsAt))}</small></td>
      <td><span class="status-pill ${escapeHtml(status)}">${escapeHtml(statusLabel(reservation.status))}</span></td>
      <td>${canCancel ? `<button class="cancel-button" type="button" data-cancel-reservation="${escapeHtml(reservation.id)}">Cancel</button>` : ""}</td>
    </tr>`;
  }).join("");
}

async function loadWorkspace() {
  const results = await Promise.allSettled([fetchJson(API.resources), fetchJson(API.reservations)]);
  if (results[0].status === "fulfilled") {
    state.resources = Array.isArray(results[0].value) ? results[0].value : [];
    setError(elements.resourceError, "");
    renderResources();
  } else {
    setError(elements.resourceError, "");
    elements.resourceGrid.replaceChildren();
  }
  if (results[1].status === "fulfilled") {
    state.reservations = Array.isArray(results[1].value) ? results[1].value : [];
    setError(elements.reservationError, "");
    renderReservations();
  } else {
    setError(elements.reservationError, "");
    elements.reservationList.replaceChildren();
  }
  renderStats();
}

function setHealthStatus(element, status) {
  const normalized = status === "UP" ? "UP" : "DOWN";
  element.textContent = normalized;
  element.closest(".health-row").dataset.status = normalized.toLowerCase();
}

async function loadHealth() {
  try {
    const response = await fetch(API.health, {
      headers: { Accept: "application/json" },
      cache: "no-store"
    });
    const health = await response.json();
    if (!health || !["UP", "DOWN"].includes(health.status) || !Array.isArray(health.checks)) {
      throw new Error("Invalid health response");
    }

    setHealthStatus(elements.healthApplication, health.status);
    const databaseCheck = health.checks.find(check =>
      /database|datasource/i.test(check.name || "")
    );
    setHealthStatus(elements.healthDatabase, databaseCheck?.status);
  } catch {
    setHealthStatus(elements.healthApplication, "DOWN");
    setHealthStatus(elements.healthDatabase, "DOWN");
  }
}

function openBooking(resourceId) {
  const resource = state.resources.find(item => String(item.id) === String(resourceId));
  if (!resource || resource.status !== "AVAILABLE") return;
  elements.bookingForm.reset();
  elements.bookingResourceId.value = resource.id;
  elements.selectedResource.innerHTML = `<span class="resource-illustration ${escapeHtml(resource.type).toLowerCase()}">${icons[resource.type] || icons.EQUIPMENT}</span><span><strong>${escapeHtml(resource.name)}</strong><small>${escapeHtml(resourceType(resource.type))}</small></span>`;
  const now = new Date();
  now.setSeconds(0, 0);
  now.setMinutes(Math.ceil(now.getMinutes() / 15) * 15);
  if (now <= new Date()) now.setMinutes(now.getMinutes() + 15);
  elements.startsAt.min = localDateTimeValue(now);
  elements.endsAt.min = localDateTimeValue(now);
  const start = new Date(now);
  start.setHours(start.getHours() + 1);
  const end = new Date(start);
  end.setHours(end.getHours() + 1);
  elements.startsAt.value = localDateTimeValue(start);
  elements.endsAt.value = localDateTimeValue(end);
  elements.formMessage.hidden = true;
  elements.bookingDialog.showModal();
  elements.reservedBy.focus();
}

function setFormMessage(message) {
  elements.formMessage.textContent = message;
  elements.formMessage.hidden = !message;
}

async function submitBooking(event) {
  event.preventDefault();
  const start = new Date(elements.startsAt.value);
  const end = new Date(elements.endsAt.value);
  if (!(start < end)) {
    setFormMessage("The end time must be after the start time.");
    elements.endsAt.focus();
    return;
  }
  const payload = {
    rentalItemId: Number(elements.bookingResourceId.value),
    startsAt: elements.startsAt.value,
    endsAt: elements.endsAt.value,
    reservedBy: elements.reservedBy.value.trim()
  };
  elements.submitBooking.disabled = true;
  elements.submitBooking.textContent = "Booking...";
  setFormMessage("");
  try {
    await fetchJson(API.createReservation, { method: "POST", body: JSON.stringify(payload) });
    elements.bookingDialog.close();
    showToast("Your reservation is confirmed. Have a great one!");
    await loadWorkspace();
  } catch (error) {
    setFormMessage(error.message || "Could not create reservation. Please try again.");
  } finally {
    elements.submitBooking.disabled = false;
    elements.submitBooking.innerHTML = 'Confirm booking <span aria-hidden="true">→</span>';
  }
}

async function cancelReservation(id, button) {
  if (!window.confirm("Cancel this reservation?")) return;
  button.disabled = true;
  button.textContent = "Cancelling...";
  try {
    await fetchJson(API.reservationStatus(id, "CANCELED"), { method: "PATCH" });
    showToast("Reservation cancelled.");
    await loadWorkspace();
  } catch (error) {
    showToast(error.message || "Could not cancel this reservation.", true);
    button.disabled = false;
    button.textContent = "Cancel";
  }
}

document.querySelector("#today-label").textContent =
  new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric" }).format(new Date());
document.querySelector("#current-year").textContent = String(new Date().getFullYear());
elements.resourceGrid.addEventListener("click", event => {
  const button = event.target.closest("[data-book-resource]");
  if (button) openBooking(button.dataset.bookResource);
});
elements.reservationList.addEventListener("click", event => {
  const button = event.target.closest("[data-cancel-reservation]");
  if (button) void cancelReservation(button.dataset.cancelReservation, button);
});
document.querySelectorAll(".filter-chip").forEach(button => {
  button.addEventListener("click", () => {
    state.filter = button.dataset.filter;
    document.querySelectorAll(".filter-chip").forEach(chip => {
      const selected = chip === button;
      chip.classList.toggle("is-selected", selected);
      chip.setAttribute("aria-pressed", String(selected));
    });
    renderResources();
  });
});
elements.resourceSearch.addEventListener("input", event => {
  state.search = event.target.value;
  renderResources();
});
elements.resourceSearch.addEventListener("keydown", event => {
  if (event.key === "Escape") {
    elements.resourceSearch.value = "";
    state.search = "";
    renderResources();
  }
});
document.addEventListener("keydown", event => {
  if (event.key === "/" && !["INPUT", "TEXTAREA"].includes(document.activeElement.tagName)) {
    event.preventDefault();
    elements.resourceSearch.focus();
  }
});
elements.bookingForm.addEventListener("submit", submitBooking);
document.querySelector("#dialog-close").addEventListener("click", () => elements.bookingDialog.close());
elements.bookingDialog.addEventListener("click", event => {
  if (event.target === elements.bookingDialog) elements.bookingDialog.close();
});
elements.startsAt.addEventListener("change", () => {
  elements.endsAt.min = elements.startsAt.value || elements.endsAt.min;
  if (elements.endsAt.value && elements.endsAt.value <= elements.startsAt.value) {
    const nextHour = new Date(elements.startsAt.value);
    nextHour.setHours(nextHour.getHours() + 1);
    elements.endsAt.value = localDateTimeValue(nextHour);
  }
});
void loadWorkspace();
void loadHealth();
window.setInterval(() => void loadHealth(), 30_000);
