const API = 'http://localhost:8080/api';

// NEW: Store the token strictly in application memory
let currentAccessToken = null;
let editTaskId = null; 

async function apiFetch(endpoint, options = {}) {
    options.headers = {
        ...options.headers,
        'Content-Type': 'application/json'
    };

    if (currentAccessToken) {
        options.headers['Authorization'] = 'Bearer ' + currentAccessToken;
    }

    let response = await fetch(`${API}${endpoint}`, options);

    if (response.status === 401 || response.status === 403) {
        console.log("Token missing or expired. Attempting silent refresh...");
        
        const refreshResponse = await fetch(`${API}/auth/refresh`, {
            method: 'POST',
            credentials: 'include' 
        });

        if (refreshResponse.ok) {
            const data = await refreshResponse.json();
            currentAccessToken = data.token;
            
            options.headers['Authorization'] = 'Bearer ' + currentAccessToken;
            response = await fetch(`${API}${endpoint}`, options);
        } else {
            console.error("Session completely expired. Logging out.");
            window.location.href = 'index.html';
        }
    }
    return response;
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

async function loadUserProfile() {
    try {
        const res = await apiFetch('/users/me');
        if (res.ok) {
            const user = await res.json();
            
            // Safely inject the username into the welcome message
            document.getElementById('displayUsername').textContent = user.username;
            
            // Pre-fill the "Edit Profile" inputs so they aren't blank
            if (document.getElementById('editUsername')) {
                document.getElementById('editUsername').value = user.username;
                document.getElementById('editEmail').value = user.email;
            }
        }
    } catch (e) {
        console.error("Failed to load user profile", e);
    }
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

        // Helper function to create options safely
        const addOption = (val, text, isSelected, isDisabled) => {
            const opt = document.createElement('option');
            opt.value = val;
            opt.textContent = text;
            if (isSelected) opt.selected = true;
            if (isDisabled) opt.disabled = true;
            statusSelect.appendChild(opt);
        };

        if (t.taskStatus === 'OVERDUE') {
            addOption('OVERDUE', 'Overdue (Action Required)', true, true);
            addOption('DONE', 'Done', false, false);
        } else if (t.recurring && t.taskStatus === 'DONE') {
            addOption('DONE', 'Done', true, false);
            addOption('IN_PROGRESS', 'In Progress (Locked)', false, true);
            addOption('TO_DO', 'To Do (Locked)', false, true);
        } else if (t.recurring && t.taskStatus === 'OVERDUE') {
            addOption('OVERDUE', 'Overdue (Action Required)', true, true);
            addOption('DONE', 'Done', false, false);
        } else {
            addOption('TO_DO', 'To Do', t.taskStatus === 'TO_DO', false);
            addOption('IN_PROGRESS', 'In Progress', t.taskStatus === 'IN_PROGRESS', false);
            addOption('DONE', 'Done', t.taskStatus === 'DONE', false);
        }

        // 5. Assemble the Metadata Bar Safely
        const metaDiv = document.createElement('div');
        
        // Priority
        const pSmall = document.createElement('small');
        pSmall.textContent = 'Priority: ';
        const bPriority = document.createElement('b');
        bPriority.textContent = t.taskPriority;
        pSmall.appendChild(bPriority);
        pSmall.appendChild(document.createTextNode(' | '));
        metaDiv.appendChild(pSmall);

        // Status
        const sSmall = document.createElement('small');
        sSmall.textContent = 'Status: ';
        sSmall.appendChild(statusSelect);
        sSmall.appendChild(document.createTextNode(' | '));
        metaDiv.appendChild(sSmall);

        // Due Date
        const formattedDate = t.dueDate 
             ? new Date(t.dueDate).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) 
             : 'No Date Set';
        
        const dSmall = document.createElement('small');
        dSmall.textContent = 'Due: ';
        const bDate = document.createElement('b');
        bDate.textContent = formattedDate;
        dSmall.appendChild(bDate);
        metaDiv.appendChild(dSmall);

        // Recurring Tag
        if (t.recurring) {
            const repeatText = document.createElement('div');
            repeatText.appendChild(document.createElement('br'));
            const smallRecur = document.createElement('small');
            const bRecur = document.createElement('b');
            bRecur.textContent = 'Daily Recurring Task';
            smallRecur.appendChild(bRecur);
            repeatText.appendChild(smallRecur);
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
            
            // Safe Lock Message
            const lockMsg = document.createElement('span');
            lockMsg.appendChild(document.createElement('br'));
            const smallLock = document.createElement('small');
            const iLock = document.createElement('i');
            iLock.textContent = 'Recurring tasks cannot be edited once completed.';
            smallLock.appendChild(iLock);
            lockMsg.appendChild(smallLock);
            
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
                'Authorization': 'Bearer ' + currentAccessToken
            },
            credentials: 'include' 
        });
    } catch (e) {
        console.error("Logout request failed.");
    } finally {
        currentAccessToken = null;
        window.location.href = 'index.html';
    }
}

async function initializeDashboard() {
    // Await forces it to finish the silent refresh and save the token to memory
    await loadTasks(); 
    await loadUserProfile(); 
    await checkAdminAccess(); 
}

initializeDashboard();

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