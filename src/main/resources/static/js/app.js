// Shared JavaScript

async function apiCall(url, method = 'GET', body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(url, options);
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Request failed' }));
        throw new Error(errorData.message || 'Request failed');
    }
    if (response.status === 204) return null;
    return response.json();
}

function showToast(message, type = 'info') {
    alert(type.toUpperCase() + ": " + message);
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        showToast('Copied to clipboard!', 'success');
    }).catch(() => {
        const ta = document.createElement('textarea');
        ta.value = text;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        ta.remove();
        showToast('Copied to clipboard!', 'success');
    });
}

function togglePassword(btn) {
    const field = btn.closest('.password-field');
    const span = field.querySelector('.pw-text');
    const actualPassword = span.dataset.password;

    if (span.textContent === '••••••••') {
        span.textContent = actualPassword;
        btn.textContent = 'Hide';
    } else {
        span.textContent = '••••••••';
        btn.textContent = 'Show';
    }
}

function openModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

document.addEventListener('click', function (e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
    }
});
