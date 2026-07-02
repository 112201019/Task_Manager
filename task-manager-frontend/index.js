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
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
        });
        if (!res.ok) return alert("Login failed! Check credentials.");
        const data = await res.json();
        localStorage.setItem('jwt_token', data.token);
        window.location.href = 'tasks.html';
    } catch (e) { alert("Server error!"); }
}

async function register() {
    const payload = {
        username: document.getElementById('regUsername').value,
        email: document.getElementById('regEmail').value,
        password: document.getElementById('regPass').value
    };
    try {
        const res = await fetch(`${API_BASE}/users/register`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload)
        });
        if (res.ok) {
            alert("Registration successful! Please login.");
            toggleView('loginBox');
        } else {
            const err = await res.json();
            alert("Error: " + err.message);
        }
    } catch (e) { alert("Server error!"); }
}