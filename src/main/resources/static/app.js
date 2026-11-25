const API_BASE = '/api';

// Global state
let state = {
    services: [],
    staffs: [],
    selectedService: null,
    selectedStaff: null,
    selectedDate: null,
    selectedSlot: null,
    allBookings: [],
    currentFilter: 'ALL'
};

// Initialize on load
document.addEventListener('DOMContentLoaded', () => {
    initDateInput();
    loadServices();
    loadStaffs();
    setupForms();
});

function initDateInput() {
    const dateInput = document.getElementById('bookingDate');
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    const todayStr = `${yyyy}-${mm}-${dd}`;
    
    dateInput.min = todayStr;
    dateInput.value = todayStr;
    state.selectedDate = todayStr;

    dateInput.addEventListener('change', (e) => {
        state.selectedDate = e.target.value;
        state.selectedSlot = null;
        updateSummary();
        loadAvailableSlots();
    });
}

// Tab switching
function switchTab(tabName) {
    document.querySelectorAll('.nav-tab').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabName);
    });
    document.querySelectorAll('.tab-panel').forEach(panel => {
        panel.classList.toggle('active', panel.id === `tab-${tabName}`);
    });

    if (tabName === 'admin') {
        loadAdminBookings();
    } else {
        loadAvailableSlots();
    }
}

// Toast Notification
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 4000);
}

// Format Currency
function formatVND(amount) {
    if (amount == null) return '0 đ';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
}

// Load Services
async function loadServices() {
    const grid = document.getElementById('servicesGrid');
    try {
        const res = await fetch(`${API_BASE}/services`);
        const services = await res.json();
        state.services = services.filter(s => s.isActive !== false);

        if (state.services.length === 0) {
            grid.innerHTML = '<div class="empty-state">Chưa có dịch vụ nào.</div>';
            return;
        }

        grid.innerHTML = state.services.map(s => `
            <div class="service-item" onclick="selectService(${s.id})" id="service-${s.id}">
                <div>
                    <div class="service-name">${escapeHtml(s.name)}</div>
                    <div class="service-desc">${escapeHtml(s.description || 'Dịch vụ chuyên nghiệp tại studio')}</div>
                </div>
                <div class="service-meta">
                    <span class="service-duration">⏱ ${s.durationMinutes || 30} phút</span>
                    <span class="service-price">${formatVND(s.price)}</span>
                </div>
            </div>
        `).join('');

        // Auto select first service
        selectService(state.services[0].id);
    } catch (err) {
        grid.innerHTML = '<div class="empty-state">Không thể tải dịch vụ.</div>';
        console.error(err);
    }
}

function selectService(id) {
    state.selectedService = state.services.find(s => s.id === id);
    state.selectedSlot = null;

    document.querySelectorAll('.service-item').forEach(el => el.classList.remove('selected'));
    const activeEl = document.getElementById(`service-${id}`);
    if (activeEl) activeEl.classList.add('selected');

    updateSummary();
    loadAvailableSlots();
}

// Load Staffs
async function loadStaffs() {
    const grid = document.getElementById('staffsGrid');
    try {
        const res = await fetch(`${API_BASE}/users/staffs`);
        state.staffs = await res.json();

        if (state.staffs.length === 0) {
            grid.innerHTML = '<div class="empty-state">Chưa có thợ nào.</div>';
            return;
        }

        grid.innerHTML = state.staffs.map(st => `
            <div class="staff-chip" onclick="selectStaff(${st.id})" id="staff-${st.id}">
                <span>💈</span>
                <span>${escapeHtml(st.fullName)}</span>
            </div>
        `).join('');

        // Auto select first staff
        selectStaff(state.staffs[0].id);
    } catch (err) {
        grid.innerHTML = '<div class="empty-state">Không thể tải danh sách thợ.</div>';
        console.error(err);
    }
}

function selectStaff(id) {
    state.selectedStaff = state.staffs.find(st => st.id === id);
    state.selectedSlot = null;

    document.querySelectorAll('.staff-chip').forEach(el => el.classList.remove('selected'));
    const activeEl = document.getElementById(`staff-${id}`);
    if (activeEl) activeEl.classList.add('selected');

    updateSummary();
    loadAvailableSlots();
}

// Load Available Time Slots
async function loadAvailableSlots() {
    if (!state.selectedService || !state.selectedStaff || !state.selectedDate) return;

    const grid = document.getElementById('slotsGrid');
    grid.innerHTML = '<div class="loading-state">Đang kiểm tra lịch trống...</div>';

    try {
        const duration = state.selectedService.durationMinutes || 30;
        const url = `${API_BASE}/bookings/available-slots?staffId=${state.selectedStaff.id}&date=${state.selectedDate}&durationMinutes=${duration}`;
        const res = await fetch(url);
        const slots = await res.json();

        if (!slots || slots.length === 0) {
            grid.innerHTML = '<div class="empty-state">Không có khung giờ nào trong ngày này.</div>';
            return;
        }

        grid.innerHTML = slots.map((slot, idx) => {
            const startShort = slot.startTime.substring(0, 5);
            const endShort = slot.endTime.substring(0, 5);
            return `
                <button type="button" 
                        class="slot-btn" 
                        id="slot-${idx}"
                        ${!slot.available ? 'disabled title="Khung giờ này đã có người đặt"' : ''}
                        onclick="selectSlot(${idx}, '${slot.startTime}', '${slot.endTime}')">
                    ${startShort}
                </button>
            `;
        }).join('');
    } catch (err) {
        grid.innerHTML = '<div class="empty-state">Lỗi khi tải khung giờ.</div>';
        console.error(err);
    }
}

function selectSlot(idx, startTime, endTime) {
    state.selectedSlot = { startTime, endTime };
    document.querySelectorAll('.slot-btn').forEach(el => el.classList.remove('selected'));
    const btn = document.getElementById(`slot-${idx}`);
    if (btn) btn.classList.add('selected');
    updateSummary();
}

function updateSummary() {
    document.getElementById('sumService').textContent = state.selectedService ? state.selectedService.name : 'Chưa chọn';
    document.getElementById('sumStaff').textContent = state.selectedStaff ? state.selectedStaff.fullName : 'Chưa chọn';
    document.getElementById('sumPrice').textContent = state.selectedService ? formatVND(state.selectedService.price) : '0 đ';

    if (state.selectedSlot && state.selectedDate) {
        const start = state.selectedSlot.startTime.substring(0, 5);
        const end = state.selectedSlot.endTime.substring(0, 5);
        document.getElementById('sumTime').textContent = `${start} - ${end} (${state.selectedDate})`;
    } else {
        document.getElementById('sumTime').textContent = 'Chưa chọn giờ';
    }
}

// Setup Forms
function setupForms() {
    // Booking Form
    document.getElementById('bookingForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!state.selectedService || !state.selectedStaff || !state.selectedSlot) {
            showToast('Vui lòng chọn đầy đủ dịch vụ, thợ và khung giờ!', 'error');
            return;
        }

        const btn = document.getElementById('submitBookingBtn');
        btn.disabled = true;
        btn.textContent = 'Đang xử lý...';

        const payload = {
            customerName: document.getElementById('customerName').value.trim(),
            customerEmail: document.getElementById('customerEmail').value.trim(),
            customerPhone: document.getElementById('customerPhone').value.trim(),
            serviceId: state.selectedService.id,
            serviceName: state.selectedService.name,
            staffId: state.selectedStaff.id,
            startTime: `${state.selectedDate}T${state.selectedSlot.startTime}`,
            endTime: `${state.selectedDate}T${state.selectedSlot.endTime}`,
            totalPrice: state.selectedService.price,
            note: document.getElementById('bookingNote').value.trim()
        };

        try {
            const res = await fetch(`${API_BASE}/bookings`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await res.json();
            if (!res.ok) {
                throw new Error(data.message || 'Đặt lịch thất bại!');
            }

            showToast(`🎉 Đặt lịch thành công! Mã đơn #${data.id}`, 'success');
            document.getElementById('bookingForm').reset();
            state.selectedSlot = null;
            updateSummary();
            loadAvailableSlots();
        } catch (err) {
            showToast(`❌ ${err.message}`, 'error');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Xác nhận đặt lịch';
        }
    });

    // Add Service Form
    document.getElementById('serviceForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            name: document.getElementById('newServiceName').value.trim(),
            description: document.getElementById('newServiceDesc').value.trim(),
            durationMinutes: parseInt(document.getElementById('newServiceDuration').value),
            price: parseFloat(document.getElementById('newServicePrice').value),
            isActive: true
        };

        try {
            const res = await fetch(`${API_BASE}/services`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error('Không thể thêm dịch vụ');
            showToast('Đã thêm dịch vụ mới thành công!', 'success');
            closeServiceModal();
            document.getElementById('serviceForm').reset();
            loadServices();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
}

// Admin Dashboard Logic
async function loadAdminBookings() {
    const tbody = document.getElementById('bookingsTableBody');
    try {
        const res = await fetch(`${API_BASE}/bookings`);
        state.allBookings = await res.json();
        // Sort newest first
        state.allBookings.sort((a, b) => b.id - a.id);
        renderStats();
        renderBookingsTable();
    } catch (err) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Lỗi tải dữ liệu.</td></tr>';
        console.error(err);
    }
}

function renderStats() {
    const bookings = state.allBookings;
    document.getElementById('statTotal').textContent = bookings.length;
    document.getElementById('statPending').textContent = bookings.filter(b => b.status === 'PENDING').length;
    document.getElementById('statConfirmed').textContent = bookings.filter(b => b.status === 'CONFIRMED').length;

    const revenue = bookings
        .filter(b => b.status === 'COMPLETED')
        .reduce((sum, b) => sum + (b.totalPrice || 0), 0);
    document.getElementById('statRevenue').textContent = formatVND(revenue);
}

function filterBookings(status, btn) {
    state.currentFilter = status;
    document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
    btn.classList.add('active');
    renderBookingsTable();
}

function renderBookingsTable() {
    const tbody = document.getElementById('bookingsTableBody');
    const list = state.currentFilter === 'ALL'
        ? state.allBookings
        : state.allBookings.filter(b => b.status === state.currentFilter);

    if (list.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Không có đơn đặt lịch nào.</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(b => {
        const dateStr = b.startTime ? b.startTime.replace('T', ' ').substring(0, 16) : '';
        return `
            <tr>
                <td><strong>#${b.id}</strong></td>
                <td>
                    <div><strong>${escapeHtml(b.customerName)}</strong></div>
                    <div style="font-size:12px;color:#64748b">${escapeHtml(b.customerPhone)}</div>
                </td>
                <td>
                    <div>${escapeHtml(b.serviceName)}</div>
                    ${b.note ? `<div style="font-size:12px;color:#64748b">📝 ${escapeHtml(b.note)}</div>` : ''}
                </td>
                <td>${dateStr}</td>
                <td><strong>${formatVND(b.totalPrice)}</strong></td>
                <td><span class="badge badge-${b.status}">${b.status}</span></td>
                <td>
                    <div class="action-group">
                        ${b.status === 'PENDING' ? `<button class="btn-sm btn-confirm" onclick="updateStatus(${b.id}, 'confirm')">Duyệt</button>` : ''}
                        ${b.status === 'CONFIRMED' ? `<button class="btn-sm btn-complete" onclick="updateStatus(${b.id}, 'complete')">Hoàn thành</button>` : ''}
                        ${(b.status === 'PENDING' || b.status === 'CONFIRMED') ? `<button class="btn-sm btn-cancel" onclick="updateStatus(${b.id}, 'cancel')">Hủy</button>` : ''}
                        <button class="btn-sm btn-delete" onclick="deleteBooking(${b.id})">Xóa</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

async function updateStatus(id, action) {
    try {
        const res = await fetch(`${API_BASE}/bookings/${id}/${action}`, { method: 'PATCH' });
        if (!res.ok) throw new Error('Không thể cập nhật trạng thái');
        showToast(`Đã cập nhật đơn #${id} thành công!`, 'success');
        loadAdminBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteBooking(id) {
    if (!confirm(`Bạn có chắc muốn xóa vĩnh viễn đơn #${id}?`)) return;
    try {
        const res = await fetch(`${API_BASE}/bookings/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Không thể xóa đơn');
        showToast(`Đã xóa đơn #${id}`, 'success');
        loadAdminBookings();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Modal helpers
function openServiceModal() {
    document.getElementById('serviceModal').classList.remove('hidden');
}

function closeServiceModal() {
    document.getElementById('serviceModal').classList.add('hidden');
}

// XSS protection
function escapeHtml(text) {
    if (!text) return '';
    return text.toString()
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
