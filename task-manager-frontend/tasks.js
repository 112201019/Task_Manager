const API = 'http://localhost:8080/api';
if (!localStorage.getItem('jwt_token')) window.location.href = 'index.html';
let editTaskId = null;

async function apiFetch(endpoint, options = {}) {
    let token = localStorage.getItem('jwt_token');
    
    options.headers = {
        ...options.headers,
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    };

    let response = await fetch(`${API}${endpoint}`, options);

    if (response.status === 401 || response.status === 403) {
        console.log("Access token expired. Attempting silent refresh...");
        
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
            console.error("Session completely expired.");
            localStorage.removeItem('jwt_token');
            window.location.href = 'index.html';
        }
    }
    return response;
}

// --- JWT DECODER ---
try {
    const token = localStorage.getItem('jwt_token');
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
        const res = await apiFetch('/admin/dashboard');
        if (res.ok) {
            document.getElementById('adminBtn').style.display = 'inline-block';
        }
    } catch (e) { console.error(e); }
}

function toggleDateInput() {
    const isRecurring = document.getElementById('isRecurring').checked;
    if (isRecurring) {
        document.getElementById('dueDate').style.display = 'none';
        document.getElementById('dueTime').style.display = 'block';
    } else {
        document.getElementById('dueDate').style.display = 'block';
        document.getElementById('dueTime').style.display = 'none';
    }
}

// --- TASKS API ---
async function loadTasks() {
    const res = await apiFetch('/tasks/getall');
    if (!res.ok) return;
    let tasks = await res.json();
    
    // Sorting Algorithm
    const priorityWeight = { 'EMERGENCY': 1, 'HIGH': 2, 'NORMAL': 3, 'LOW': 4 };
    tasks.sort((a, b) => {
        const aIsFinished = (a.taskStatus === 'DONE' || a.taskStatus === 'OVERDUE') ? 1 : 0;
        const bIsFinished = (b.taskStatus === 'DONE' || b.taskStatus === 'OVERDUE') ? 1 : 0;
        if (aIsFinished !== bIsFinished) return aIsFinished - bIsFinished;
        
        const pA = priorityWeight[a.taskPriority] || 5;
        const pB = priorityWeight[b.taskPriority] || 5;
        if (pA !== pB) return pA - pB;
        
        if (!a.dueDate && !b.dueDate) return 0;
        if (!a.dueDate) return 1;
        if (!b.dueDate) return -1;
        return new Date(a.dueDate) - new Date(b.dueDate);
    });
    
    const list = document.getElementById('taskList');
    list.innerHTML = ''; // Safe to clear with innerHTML
    
    if (!tasks.length) {
        list.innerHTML = '<p>No tasks found.</p>';
        return;
    }
    
    tasks.forEach(t => {
        // 1. Create the container
        const card = document.createElement('div');
        card.className = 'task-card';

        // 2. Create the Title (SAFE TEXT CONTENT)
        const titleEl = document.createElement('h4');
        titleEl.style.margin = '0 0 10px 0';
        titleEl.textContent = t.title;

        // 3. Create the Description (SAFE TEXT CONTENT)
        const descEl = document.createElement('p');
        descEl.style.margin = '0 0 10px 0';
        descEl.textContent = t.description || '';

        // 4. Create the Status Dropdown Safely
        const statusSelect = document.createElement('select');
        statusSelect.className = 'inline-btn';
        statusSelect.addEventListener('change', (e) => updateStatus(t.taskId, e.target.value));

        if (t.taskStatus === 'OVERDUE') {
            statusSelect.innerHTML = `
            <option value="OVERDUE" disabled selected>Overdue (Action Required)</option>
            <option value="DONE">Done</option>`;
        } else if (t.recurring && t.taskStatus === 'DONE') {
            statusSelect.innerHTML = `
            <option value="DONE" selected>Done</option>
            <option value="IN_PROGRESS" disabled>In Progress (Locked)</option>
            <option value="TO_DO" disabled>To Do (Locked)</option>`;
        } else if (t.recurring && t.taskStatus === 'OVERDUE') {
            statusSelect.innerHTML = `
            <option value="OVERDUE" disabled selected>Overdue (Action Required)</option>
            <option value="DONE">Done</option>`;
        } else {
            statusSelect.innerHTML = `
            <option value="TO_DO" ${t.taskStatus === 'TO_DO' ? 'selected' : ''}>To Do</option>
            <option value="IN_PROGRESS" ${t.taskStatus === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
            <option value="DONE" ${t.taskStatus === 'DONE' ? 'selected' : ''}>Done</option>`;
        }

        // 5. Assemble the Metadata Bar
        const metaDiv = document.createElement('div');
        const pSmall = document.createElement('small');
        pSmall.innerHTML = `Priority: <b>${t.taskPriority}</b> | `; 
        metaDiv.appendChild(pSmall);

        const sSmall = document.createElement('small');
        sSmall.textContent = 'Status: ';
        sSmall.appendChild(statusSelect);
        sSmall.appendChild(document.createTextNode(' | '));
        metaDiv.appendChild(sSmall);

        const formattedDate = t.dueDate 
            ? new Date(t.dueDate).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) 
            : 'No Date Set';
        const dSmall = document.createElement('small');
        dSmall.innerHTML = `Due: <b>${formattedDate}</b>`;
        metaDiv.appendChild(dSmall);

        if (t.recurring) {
            const repeatText = document.createElement('div');
            repeatText.innerHTML = '<br><small><b>Daily Recurring Task</b></small>';
            metaDiv.appendChild(repeatText);
        }

        const hr = document.createElement('hr');
        const controlsDiv = document.createElement('div');

        const delBtn = document.createElement('button');
        delBtn.className = 'inline-btn';
        delBtn.textContent = 'Delete';
        delBtn.style.marginLeft = '5px';
        delBtn.addEventListener('click', () => deleteTask(t.taskId));

        // 6. Create Buttons Safely
        if (t.recurring && t.taskStatus === 'DONE') {
            const lockBtn = document.createElement('button');
            lockBtn.className = 'inline-btn';
            lockBtn.disabled = true;
            lockBtn.style = 'background-color: #ddd; color: #888; cursor: not-allowed; border-color: #ccc;';
            lockBtn.textContent = 'Locked';
            
            const lockMsg = document.createElement('span');
            lockMsg.innerHTML = '<br><small><i>Recurring tasks cannot be edited once completed.</i></small>';
            
            controlsDiv.appendChild(lockBtn);
            controlsDiv.appendChild(delBtn);
            controlsDiv.appendChild(lockMsg);
        } else {
            const editBtn = document.createElement('button');
            editBtn.className = 'inline-btn';
            editBtn.textContent = 'Edit';
            editBtn.addEventListener('click', () => {
                setupEdit(t.taskId, t.title, t.description, t.taskPriority, t.recurring, t.dueDate);
            });

            controlsDiv.appendChild(editBtn);
            controlsDiv.appendChild(delBtn);
        }

        // 7. Inject everything into the card, then the list
        card.appendChild(titleEl);
        card.appendChild(descEl);
        card.appendChild(metaDiv);
        card.appendChild(hr);
        card.appendChild(controlsDiv);
        
        list.appendChild(card);
    });
}

function setupEdit(id, title, desc, priority, isRecurring, dueDate) {
    editTaskId = id;
    document.getElementById('formTitle').innerText = "Edit Task";
    document.getElementById('aboutTask').innerText = "Edit the selected Task here.";
    document.getElementById('title').value = title;
    document.getElementById('desc').value = desc;
    document.getElementById('priority').value = priority;
    
    // LOCK THE CHECKBOX
    const recurCb = document.getElementById('isRecurring');
    recurCb.checked = (isRecurring === true);
    recurCb.disabled = true;
    document.getElementById('recurLabel').innerText = isRecurring ? "Daily Recurring Task (Uneditable)" : "Standard Task (Uneditable)";
    
    // Show Cancel button & change Save text
    document.getElementById('cancelBtn').style.display = 'block';
    document.getElementById('saveBtn').innerText = 'Update Task';
    
    if (isRecurring === true) {
        document.getElementById('dueDate').style.display = 'none';
        document.getElementById('dueTime').style.display = 'block';
        if (dueDate && dueDate !== 'null' && dueDate !== 'undefined') {
            const timePart = dueDate.split('T')[1]?.substring(0, 5) || "";
            document.getElementById('dueTime').value = timePart;
        } else {
            document.getElementById('dueTime').value = '';
        }
    } else {
        document.getElementById('dueDate').style.display = 'block';
        document.getElementById('dueTime').style.display = 'none';
        if (dueDate && dueDate !== 'null' && dueDate !== 'undefined') {
            document.getElementById('dueDate').value = dueDate.substring(0, 16);
        } else {
            document.getElementById('dueDate').value = '';
        }
    }
    window.scrollTo(0, 0);
}

function cancelEdit() {
    editTaskId = null;
    document.getElementById('formTitle').innerText = "Add New Task";
    document.getElementById('title').value = '';
    document.getElementById('desc').value = '';
    document.getElementById('dueDate').value = '';
    document.getElementById('dueTime').value = '';
    
    // UNLOCK THE CHECKBOX
    document.getElementById('isRecurring').checked = false;
    document.getElementById('isRecurring').disabled = false;
    document.getElementById('recurLabel').innerText = "Make this a Daily Recurring Task";
    
    // Hide Cancel button & reset Save text
    document.getElementById('cancelBtn').style.display = 'none';
    document.getElementById('saveBtn').innerText = 'Save Task';
    
    toggleDateInput(); // Reset UI
}

async function saveTask() {
    const titleVal = document.getElementById('title').value.trim();
    if (!titleVal) {
        showMessage("Wait! Your task needs a title before saving.");
        return;
    }

    const isRecurring = document.getElementById('isRecurring').checked;
    let dueDateValue = null;

    if (isRecurring) {
        const timeVal = document.getElementById('dueTime').value;
        if (timeVal) {
            const now = new Date();
            const year = now.getFullYear();
            const month = String(now.getMonth() + 1).padStart(2, '0');
            const date = String(now.getDate()).padStart(2, '0');
            dueDateValue = `${year}-${month}-${date}T${timeVal}:00`;
        }
    } else {
        dueDateValue = document.getElementById('dueDate').value || null;
    }

    const payload = {
        title: titleVal,
        description: document.getElementById('desc').value.trim(),
        taskPriority: document.getElementById('priority').value,
        recurring: isRecurring,
        dueDate: dueDateValue
    };
    
    const endpoint = editTaskId ? `/tasks/edit/${editTaskId}` : `/tasks/save`;
    const method = editTaskId ? 'PUT' : 'POST';
    
    await apiFetch(endpoint, { method, body: JSON.stringify(payload) });
    
    cancelEdit();
    loadTasks();
}

async function updateStatus(id, status) {
    await apiFetch(`/tasks/update-status/${id}`, { method: 'PATCH', body: JSON.stringify({ taskStatus: status }) });
    loadTasks();
}

async function deleteTask(id) {
    if (confirm("Delete task?")) {
        await apiFetch(`/tasks/delete/${id}`, { method: 'DELETE' });
        loadTasks();
    }
}

// --- USER API ---
async function updateProfile() {
    const payload = { username: document.getElementById('editUsername').value, email: document.getElementById('editEmail').value };
    const res = await apiFetch(`/users/edit`, { method: 'PATCH', body: JSON.stringify(payload) });
    if (res.ok) {
        showMessage("Profile updated! Please log in again to apply changes.");
        setTimeout(() => logout(), 2000);
    } else {
        showMessage("Update failed. Username or email might be taken.", "error");
    }
}

async function changePassword() {
    const payload = { oldPassword: document.getElementById('oldPass').value, newPassword: document.getElementById('newPass').value };
    const res = await apiFetch(`/users/change-password`, { method: 'PATCH', body: JSON.stringify(payload) });
    if (res.ok) {
        document.getElementById('oldPass').value = '';
        document.getElementById('newPass').value = '';
        showMessage("Password changed successfully!");
    } else {
        showMessage("Failed. Check old password.", "error");
    }
}

async function deleteAccount() {
    if (confirm("WARNING: This will permanently delete your account and all tasks!")) {
        await apiFetch(`/users/delete`, { method: 'DELETE' });
        logout();
    }
}

async function logout() {
    try {
        await fetch(`${API}/auth/logout`, { 
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('jwt_token')
            },
            credentials: 'include' 
        });
    } catch (e) {
        console.error("Logout request failed, cleaning up locally.");
    } finally {
        localStorage.removeItem('jwt_token');
        window.location.href = 'index.html';
    }
}

checkAdminAccess();
loadTasks();

// --- TOAST MESSENGER ---
function showMessage(message, type = 'success') {
    const toast = document.getElementById('notificationBox');
    toast.innerText = message;
    
    // Green for success, Red for errors
    toast.style.backgroundColor = type === 'error' ? '#ff4d4d' : '#4CAF50';
    toast.style.display = 'block';
    
    // Hide after 3 seconds
    setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}