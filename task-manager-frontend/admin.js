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

    // Safely Build Users Table
    const uTable = document.getElementById('usersTable');
    uTable.innerHTML = ''; // Safe to wipe
    data.users.forEach(u => {
        const tr = document.createElement('tr');
        
        const tdId = document.createElement('td');
        tdId.innerHTML = `<small>${u.userId}</small>`; // UUID is system generated

        const tdUser = document.createElement('td');
        tdUser.textContent = u.username; // SAFE TEXT CONTENT

        const tdEmail = document.createElement('td');
        tdEmail.textContent = u.email; // SAFE TEXT CONTENT

        const tdAction = document.createElement('td');
        
        const editBtn = document.createElement('button');
        editBtn.className = 'inline-btn';
        editBtn.textContent = 'Edit';
        editBtn.addEventListener('click', () => promptEditUser(u.userId, u.username, u.email));

        const delBtn = document.createElement('button');
        delBtn.className = 'inline-btn';
        delBtn.textContent = 'Delete';
        delBtn.style.marginLeft = '5px';
        delBtn.addEventListener('click', () => adminDeleteUser(u.userId));

        tdAction.appendChild(editBtn);
        tdAction.appendChild(delBtn);

        tr.appendChild(tdId);
        tr.appendChild(tdUser);
        tr.appendChild(tdEmail);
        tr.appendChild(tdAction);

        uTable.appendChild(tr);
    });

    // Safely Build Tasks Table
    const tTable = document.getElementById('tasksTable');
    tTable.innerHTML = '';
    data.tasks.forEach(t => {
        const tr = document.createElement('tr');

        const tdId = document.createElement('td');
        tdId.innerHTML = `<small>${t.taskId}</small>`; // UUID

        const tdTitle = document.createElement('td');
        tdTitle.textContent = t.title; // SAFE TEXT CONTENT

        const tdOwner = document.createElement('td');
        tdOwner.innerHTML = `<small>${t.userId}</small>`; // UUID

        const tdAction = document.createElement('td');
        
        const delBtn = document.createElement('button');
        delBtn.className = 'inline-btn';
        delBtn.textContent = 'Delete';
        delBtn.addEventListener('click', () => adminDeleteTask(t.taskId));

        tdAction.appendChild(delBtn);

        tr.appendChild(tdId);
        tr.appendChild(tdTitle);
        tr.appendChild(tdOwner);
        tr.appendChild(tdAction);

        tTable.appendChild(tr);
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