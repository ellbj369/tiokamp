async function saveScore(eventNum) {
    const input    = document.getElementById(`input-${eventNum}`);
    const feedback = document.getElementById(`feedback-${eventNum}`);
    const current  = document.getElementById(`current-${eventNum}`);
    const btn      = document.querySelector(`#card-${eventNum} .btn-save`);

    // Accept Swedish decimal comma as well as dot
    const raw   = String(input.value).trim().replace(',', '.');
    const value = parseFloat(raw);

    if (raw === '' || isNaN(value) || value < 0 || value > 100000) {
        showFeedback(feedback, 'Ange ett giltigt tal (0–100 000)', 'error');
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
            current.textContent = String(value).replace('.', ',');
            input.value         = '';
            showFeedback(feedback, '✓ Snyggt!', 'success');
            showToast('Resultatet är sparat!');
        } else {
            showFeedback(feedback, data.error || 'Det gick inte att spara', 'error');
        }
    } catch (err) {
        showFeedback(feedback, 'Nätverksfel — försök igen', 'error');
    } finally {
        btn.disabled    = false;
        btn.textContent = 'Spara';
    }
}

function showFeedback(el, msg, type) {
    el.textContent  = msg;
    el.className    = `card-feedback card-feedback--${type}`;
    setTimeout(() => { el.textContent = ''; el.className = 'card-feedback'; }, 4000);
}

let toastTimeout;
function showToast(msg) {
    const toast = document.getElementById('saveToast');
    toast.textContent = msg;
    toast.classList.add('toast--visible');
    clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => toast.classList.remove('toast--visible'), 3000);
}

document.querySelectorAll('.btn-save').forEach(btn => {
    btn.addEventListener('click', () => saveScore(parseInt(btn.dataset.event)));
});

// Event description popup (?-buttons)
const infoDialog = document.getElementById('eventInfoDialog');
document.querySelectorAll('.btn-info').forEach(btn => {
    btn.addEventListener('click', () => {
        if (infoDialog && infoDialog.showModal) {
            document.getElementById('eventInfoIcon').textContent  = btn.dataset.icon;
            document.getElementById('eventInfoTitle').textContent = btn.dataset.name;
            document.getElementById('eventInfoText').textContent  = btn.dataset.description;
            infoDialog.showModal();
        } else {
            alert(btn.dataset.name + '\n\n' + btn.dataset.description);
        }
    });
});
if (infoDialog) {
    document.getElementById('eventInfoClose').addEventListener('click', () => infoDialog.close());
    // tap on the dimmed backdrop closes too
    infoDialog.addEventListener('click', e => { if (e.target === infoDialog) infoDialog.close(); });
}

// Allow pressing Enter to save in any input
document.querySelectorAll('.card-input').forEach(input => {
    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            saveScore(parseInt(input.id.replace('input-', '')));
        }
    });
});
