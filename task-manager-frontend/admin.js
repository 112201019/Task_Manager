const API = 'http://localhost:8080/api';
const token = localStorage.getItem('jwt_token');
if (!token) window.location.href = 'index.html';
const headers = { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token };

let currentEditUserId = null;
let currentEditTaskId = null;

async function loadDashboard() {
    const res = await fetch(`${API}/admin/dashboard`, { headers });
    
    if (res.status === 403) {
        document.getElementById('adminContent').style.display = 'none';
        document.getElementById('errorMsg').style.display = 'block';
        return;
    }
    
    document.getElementById('adminContent').style.display = 'flex';
    const data = await res.json();

    const uTable = document.getElementById('usersTable');
    uTable.innerHTML = '';
    data.users.forEach(u => {
        uTable.innerHTML += `<tr>
            <td><small>${u.userId}</small></td>
            <td>${u.username}</td>
            <td>${u.email}</td>
            <td>
                <button class="inline-btn" onclick="promptEditUser('${u.userId}', '${u.username}', '${u.email}')">Edit</button>
                <button class="inline-btn" onclick="adminDeleteUser('${u.userId}')">Delete</button>
            </td>
        </tr>`;
    });

    const tTable = document.getElementById('tasksTable');
    tTable.innerHTML = '';
    data.tasks.forEach(t => {
        tTable.innerHTML += `<tr>
            <td><small>${t.taskId}</small></td>
            <td>${t.title}</td>
            <td><small>${t.userId}</small></td>
            <td>
                <button class="inline-btn" onclick="adminDeleteTask('${t.taskId}')">Delete</button>
            </td>
        </tr>`;
    });
}

// --- EDIT USER LOGIC ---
function promptEditUser(id, username, email) {
    currentEditUserId = id;
    document.getElementById('adminEditUsername').value = username;
    document.getElementById('adminEditEmail').value = email;
    document.getElementById('adminEditUserBox').style.display = 'block';
    window.scrollTo(0, 0);
}

async function saveAdminUser() {
    const payload = { username: document.getElementById('adminEditUsername').value, email: document.getElementById('adminEditEmail').value };
    await fetch(`${API}/admin/edit-user/${currentEditUserId}`, { method: 'PATCH', headers, body: JSON.stringify(payload) });
    cancelAdminEdit('adminEditUserBox');
    loadDashboard();
}

async function adminDeleteUser(userId) {
    if (confirm("Delete this user AND all their tasks system-wide?")) {
        await fetch(`${API}/admin/delete-user/${userId}`, { method: 'DELETE', headers });
        loadDashboard();
    }
}

async function saveAdminTask() {
    const payload = {
        title: document.getElementById('adminEditTaskTitle').value,
        description: document.getElementById('adminEditTaskDesc').value,
        taskPriority: document.getElementById('adminEditTaskPriority').value,
        taskStatus: document.getElementById('adminEditTaskStatus').value,
        dueDate: document.getElementById('adminEditTaskDueDate').value
    };
    await fetch(`${API}/admin/edit-task/${currentEditTaskId}`, { method: 'PUT', headers, body: JSON.stringify(payload) });
    cancelAdminEdit('adminEditTaskBox');
    loadDashboard();
}

async function adminDeleteTask(taskId) {
    if (confirm("Delete this task?")) {
        await fetch(`${API}/admin/delete-task/${taskId}`, { method: 'DELETE', headers });
        loadDashboard();
    }
}

function cancelAdminEdit(boxId) {
    document.getElementById(boxId).style.display = 'none';
}

loadDashboard();