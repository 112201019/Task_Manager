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
    const res = await fetch(`${API}/tasks/getall`, { headers });
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
    list.innerHTML = tasks.length ? '' : '<p>No tasks found.</p>';
    
    tasks.forEach(t => {
        let statusDropdownHtml = '';
        if (t.taskStatus === 'OVERDUE') {
            statusDropdownHtml = `
            <option value="OVERDUE" disabled selected>Overdue (Action Required)</option>
            <option value="DONE">Done</option>
            `;
        } else {
            if(t.recurring && t.taskStatus === 'DONE'){
                statusDropdownHtml = `
                <option value="DONE" selected>Done</option>
                <option value="IN_PROGRESS" disabled>In Progress (Locked)</option>
                <option value="TO_DO" disabled>To Do (Locked)</option>
                `;       
            }
            else if (t.recurring && t.taskStatus === 'OVERDUE') {
                statusDropdownHtml = `
                <option value="OVERDUE" disabled selected>Overdue (Action Required)</option>
                <option value="DONE">Done</option>
                `;
            }
            else{
                statusDropdownHtml = `
                <option value="TO_DO" ${t.taskStatus === 'TO_DO' ? 'selected' : ''}>To Do</option>
                <option value="IN_PROGRESS" ${t.taskStatus === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                <option value="DONE" ${t.taskStatus === 'DONE' ? 'selected' : ''}>Done</option>
                `;
            }
            
        }
        var repeatText = t.recurring ? `<br><small><b>Daily Recurring Task</b></small>` : '';
        
        const formattedDate = t.dueDate 
            ? new Date(t.dueDate).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) 
            : 'No Date Set';


        let editButtonHtml = '';
        if (t.recurring && t.taskStatus === 'DONE') {
            editButtonHtml = `<button class="inline-btn" disabled style="background-color: #ddd; color: #888; cursor: not-allowed; border-color: #ccc;">Locked</button>`;
            repeatText = `<br><small><i>Recurring tasks cannot be edited once completed.</i></small>`;
        } else {
            editButtonHtml = `<button class="inline-btn" onclick="setupEdit('${t.taskId}', '${t.title.replace(/'/g, "\\'")}', '${(t.description || '').replace(/'/g, "\\'")}', '${t.taskPriority}', ${t.recurring}, '${t.dueDate}')">Edit</button>`;
        }

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
                <small>Due: <b>${formattedDate}</b></small>
                ${repeatText}
                <hr>
                ${editButtonHtml} <!-- Dynamically injected Edit or Locked button -->
                <button class="inline-btn" onclick="deleteTask('${t.taskId}')">Delete</button>
            </div>
        `;
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

// NEW: Easily exit edit mode and unlock the form
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

// FIX: saveTask now uses cancelEdit() to clean up after saving
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
    
    const url = editTaskId ? `${API}/tasks/edit/${editTaskId}` : `${API}/tasks/save`;
    const method = editTaskId ? 'PUT' : 'POST';
    
    await fetch(url, { method, headers, body: JSON.stringify(payload) });
    
    cancelEdit(); // Perfectly resets and unlocks the form!
    loadTasks();
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
        showMessage("Profile updated! Please log in again to apply changes.");
        setTimeout(() => logout(), 2000);
    } else {
        showMessage("Update failed. Username or email might be taken.");
    }
}

async function changePassword() {
    const payload = { oldPassword: document.getElementById('oldPass').value, newPassword: document.getElementById('newPass').value };
    const res = await fetch(`${API}/users/change-password`, { method: 'PATCH', headers, body: JSON.stringify(payload) });
    if (res.ok) {
        document.getElementById('oldPass').value = '';
        document.getElementById('newPass').value = '';
        showMessage("Password changed successfully!");
    } else {
        showMessage("Failed. Check old password.");
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