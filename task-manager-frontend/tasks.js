const API = 'http://localhost:8080/api';
const token = localStorage.getItem('jwt_token');

if (!token) window.location.href = 'index.html';

const headers = { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token };
let editTaskId = null;

// --- JWT DECODER ---
try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    document.getElementById('displayUsername').innerText = payload.username;
    document.getElementById('editUsername').value = payload.username;
    document.getElementById('editEmail').value = payload.sub; 
} catch (e) {
    console.error("Invalid token format");
}

// --- ADMIN CHECK ---
async function checkAdminAccess() {
    try {
        const res = await fetch(`${API}/admin/dashboard`, { headers });
        if (res.ok) {
            document.getElementById('adminBtn').style.display = 'inline-block';
        }
    } catch (e) { console.error(e); }
}

// --- TASKS API ---
async function loadTasks() {
    // Simplified to ONLY hit /getall
    const res = await fetch(`${API}/tasks/getall`, { headers });
    if (!res.ok) return;
    const tasks = await res.json();
    
    // ==========================================
    // NEW: ADVANCED SORTING ALGORITHM
    // ==========================================
    const priorityWeight = { 'EMERGENCY': 1, 'HIGH': 2, 'NORMAL': 3, 'LOW': 4 };
    
    tasks.sort((a, b) => {
        // 1. Put DONE and OVERDUE at the very bottom
        const aIsFinished = (a.taskStatus === 'DONE' || a.taskStatus === 'OVERDUE') ? 1 : 0;
        const bIsFinished = (b.taskStatus === 'DONE' || b.taskStatus === 'OVERDUE') ? 1 : 0;
        if (aIsFinished !== bIsFinished) return aIsFinished - bIsFinished;

        // 2. Sort by Priority (Emergency -> High -> Normal -> Low)
        const pA = priorityWeight[a.taskPriority] || 5;
        const pB = priorityWeight[b.taskPriority] || 5;
        if (pA !== pB) return pA - pB;

        // 3. Sort by Due Date (Earliest dates first; tasks with no date go last)
        if (!a.dueDate && !b.dueDate) return 0;
        if (!a.dueDate) return 1;
        if (!b.dueDate) return -1;
        return new Date(a.dueDate) - new Date(b.dueDate);
    });
    // ==========================================
    
    const list = document.getElementById('taskList');
    list.innerHTML = tasks.length ? '' : '<p>No tasks found.</p>';
    
    tasks.forEach(t => {
        let statusDropdownHtml = '';
        if (t.taskStatus === 'OVERDUE') {
            statusDropdownHtml = `
                <option value="OVERDUE" disabled selected>Overdue (Action Required)</option>
                <option value="DONE">Done</option>
            `;
        } else {
            statusDropdownHtml = `
                <option value="TO_DO" ${t.taskStatus === 'TO_DO' ? 'selected' : ''}>To Do</option>
                <option value="IN_PROGRESS" ${t.taskStatus === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                <option value="DONE" ${t.taskStatus === 'DONE' ? 'selected' : ''}>Done</option>
            `;
        }

        const formattedDate = t.dueDate 
            ? new Date(t.dueDate).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) 
            : 'No Date Set';

        list.innerHTML += `
            <div class="task-card">
                <h4 style="margin:0 0 10px 0">${t.title}</h4>
                <p style="margin:0 0 10px 0">${t.description}</p>
                <small>Priority: <b>${t.taskPriority}</b></small> | 
                <small>Status: 
                    <select class="inline-btn" onchange="updateStatus('${t.taskId}', this.value)">
                        ${statusDropdownHtml}
                    </select>
                </small> | 
                <!-- FIX: Display the formatted Due Date -->
                <small>Due: <b>${formattedDate}</b></small>
                <hr>
                <button class="inline-btn" onclick="setupEdit('${t.taskId}', '${t.title}', '${t.description}', '${t.taskPriority}')">Edit</button>
                <button class="inline-btn" onclick="deleteTask('${t.taskId}')">Delete</button>
            </div>
        `;
    });
}

async function saveTask() {
    const payload = {
        title: document.getElementById('title').value,
        description: document.getElementById('desc').value,
        taskPriority: document.getElementById('priority').value,
        dueDate: document.getElementById('dueDate').value
    };
    const url = editTaskId ? `${API}/tasks/edit/${editTaskId}` : `${API}/tasks/save`;
    const method = editTaskId ? 'PUT' : 'POST';
    
    await fetch(url, { method, headers, body: JSON.stringify(payload) });
    
    editTaskId = null;
    document.getElementById('formTitle').innerText = "Add New Task";
    document.getElementById('title').value = '';
    document.getElementById('desc').value = '';
    loadTasks();
}

function setupEdit(id, title, desc, priority) {
    editTaskId = id;
    document.getElementById('formTitle').innerText = "Edit Task";
    document.getElementById('title').value = title;
    document.getElementById('desc').value = desc;
    document.getElementById('priority').value = priority;
    window.scrollTo(0, 0);
}

async function updateStatus(id, status) {
    await fetch(`${API}/tasks/update-status/${id}`, { method: 'PATCH', headers, body: JSON.stringify({ taskStatus: status }) });
    loadTasks(); 
}

async function deleteTask(id) {
    if (confirm("Delete task?")) {
        await fetch(`${API}/tasks/delete/${id}`, { method: 'DELETE', headers });
        loadTasks();
    }
}

// --- USER API ---
async function updateProfile() {
    const payload = { username: document.getElementById('editUsername').value, email: document.getElementById('editEmail').value };
    const res = await fetch(`${API}/users/edit`, { method: 'PATCH', headers, body: JSON.stringify(payload) });
    if (res.ok) {
        alert("Profile updated! Please log in again to apply changes.");
        logout();
    } else {
        alert("Update failed. Username or email might be taken.");
    }
}

async function changePassword() {
    const payload = { oldPassword: document.getElementById('oldPass').value, newPassword: document.getElementById('newPass').value };
    const res = await fetch(`${API}/users/change-password`, { method: 'PATCH', headers, body: JSON.stringify(payload) });
    if (res.ok) {
        document.getElementById('oldPass').value = '';
        document.getElementById('newPass').value = '';
        alert("Password changed successfully!");
    } else {
        alert("Failed. Check old password.");
    }
}

async function deleteAccount() {
    if (confirm("WARNING: This will permanently delete your account and all tasks!")) {
        await fetch(`${API}/users/delete`, { method: 'DELETE', headers });
        logout();
    }
}

function logout() { 
    localStorage.removeItem('jwt_token'); 
    window.location.href = 'index.html'; 
}

// Bootstrapping the application
checkAdminAccess();
loadTasks();