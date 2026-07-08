async function saveScore(eventNum) {
    const input    = document.getElementById(`input-${eventNum}`);
    const feedback = document.getElementById(`feedback-${eventNum}`);
    const current  = document.getElementById(`current-${eventNum}`);
    const btn      = document.querySelector(`#card-${eventNum} .btn-save`);
    const value    = parseFloat(input.value);

    if (isNaN(value) || value < 0) {
        showFeedback(feedback, '⚠️ Enter a valid number', 'error');
        return;
    }

    // Read CSRF token from meta tags
    const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    btn.disabled    = true;
    btn.textContent = '…';

    try {
        const res = await fetch(`/api/scores/${eventNum}`, {
            method:  'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken       
            },
            body: JSON.stringify({ value })
        });
        const data = await res.json();

        if (res.ok) {
            current.textContent = value;
            input.value         = '';
            showFeedback(feedback, '✓ Snyggt!', 'success');
            showToast(`Score saved! Your total is now ${data.total}`);
        } else {
            showFeedback(feedback, data.error || 'Fail', 'error');
        }
    } catch (err) {
        showFeedback(feedback, 'Network error — try again', 'error');
    } finally {
        btn.disabled    = false;
        btn.textContent = 'Save';
    }
}

function showFeedback(el, msg, type) {
    el.textContent  = msg;
    el.className    = `card-feedback card-feedback--${type}`;
    setTimeout(() => { el.textContent = ''; el.className = 'card-feedback'; }, 3000);
}

let toastTimeout;
function showToast(msg) {
    const toast = document.getElementById('saveToast');
    toast.textContent = msg;
    toast.classList.add('toast--visible');
    clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => toast.classList.remove('toast--visible'), 3000);
}

// Allow pressing Enter to save in any input
document.querySelectorAll('.card-input').forEach(input => {
    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            const num = input.id.replace('input-', '');
            saveScore(parseInt(num));
        }
    });
});
