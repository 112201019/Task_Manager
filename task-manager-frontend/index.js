const API_BASE = 'http://localhost:8080/api';

function toggleView(showId) {
    document.getElementById('loginBox').style.display = 'none';
    document.getElementById('registerBox').style.display = 'none';
    document.getElementById(showId).style.display = 'block';
}

async function login() {
    const payload = {
        loginIdentifier: document.getElementById('loginId').value,
        password: document.getElementById('loginPass').value
    };
    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST', 
            headers: { 'Content-Type': 'application/json' }, 
            body: JSON.stringify(payload)
        });
        if (!res.ok) {
            if (res.status === 429) {
                const errData = await res.json();
                return showMessage(errData.error, 'error'); 
            }
            else{
                return showMessage("Login failed! Check credentials.", 'error');
            }
        }
        const data = await res.json();
        localStorage.setItem('jwt_token', data.token);
        window.location.href = 'tasks.html';
    } catch (e) { 
        showMessage("Server error!", 'error'); 
    }
}

async function register() {
    const payload = {
        username: document.getElementById('regUsername').value,
        email: document.getElementById('regEmail').value,
        password: document.getElementById('regPass').value
    };
    try {
        const res = await fetch(`${API_BASE}/users/register`, {
            method: 'POST', 
            headers: { 'Content-Type': 'application/json' }, 
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            showMessage("Registration successful! Please login.");
            toggleView('loginBox');
        } else {
            const err = await res.json();
            showMessage("Error: " + err.message);
        }
    } catch (e) { showMessage("Server error!"); }
}

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