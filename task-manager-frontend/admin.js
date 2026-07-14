const API = 'http://localhost:8080/api';
if (!localStorage.getItem('jwt_token')) window.location.href = 'index.html';

let currentEditUserId = null;

async function apiFetch(endpoint, options = {}) {
    let token = localStorage.getItem('jwt_token');
    
    options.headers = {
        ...options.headers,
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    };

    let response = await fetch(`${API}${endpoint}`, options);

    if (response.status === 401 || response.status === 403) {
        const refreshResponse = await fetch(`${API}/auth/refresh`, {
            method: 'POST',
            credentials: 'include' 
        });

        if (refreshResponse.ok) {
            const data = await refreshResponse.json();
            localStorage.setItem('jwt_token', data.token);
            
            options.headers['Authorization'] = 'Bearer ' + data.token;
            response = await fetch(`${API}${endpoint}`, options);
        } else {
            localStorage.removeItem('jwt_token');
            window.location.href = 'index.html';
        }
    }
    return response;
}

async function loadDashboard() {
    const res = await apiFetch('/admin/dashboard');
    
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
        const smallUserId = document.createElement('small');
        smallUserId.textContent = u.userId;
        tdId.appendChild(smallUserId);
        const tdUser = document.createElement('td');
        tdUser.textContent = u.username; 
        const tdEmail = document.createElement('td');
        tdEmail.textContent = u.email; 
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
        const smallTaskId = document.createElement('small');
        smallTaskId.textContent = t.taskId;
        tdId.appendChild(smallTaskId);

        const tdTitle = document.createElement('td');
        tdTitle.textContent = t.title;

        const tdOwner = document.createElement('td');
        const smallOwnerId = document.createElement('small');
        smallOwnerId.textContent = t.userId;
        tdOwner.appendChild(smallOwnerId);
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
    await apiFetch(`/admin/edit-user/${currentEditUserId}`, { method: 'PATCH', body: JSON.stringify(payload) });
    cancelAdminEdit('adminEditUserBox');
    loadDashboard();
}

async function adminDeleteUser(userId) {
    if (confirm("Delete this user AND all their tasks system-wide?")) {
        await apiFetch(`/admin/delete-user/${userId}`, { method: 'DELETE' });
        loadDashboard();
    }
}

async function adminDeleteTask(taskId) {
    if (confirm("Delete this task?")) {
        await apiFetch(`/admin/delete-task/${taskId}`, { method: 'DELETE' });
        loadDashboard();
    }
}

function cancelAdminEdit(boxId) {
    document.getElementById(boxId).style.display = 'none';
}

loadDashboard();