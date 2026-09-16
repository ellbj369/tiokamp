async function refreshLatest() {
    try {
        const res = await fetch('/api/leaderboard/latest');
        if (res.status === 204) return; // no scores yet

        const d   = await res.json();
        const el  = document.getElementById('latestBanner');
        if (!el) return;

        el.querySelector('.latest-name').textContent = d.username;
        el.querySelector('.latest-detail').innerHTML = d.hidden
            ? `la en hemlig gissning i <span>${d.eventName}</span>`
            : `gjorde precis <strong>${d.eventValue}</strong> i <span>${d.eventName}</span>`;
        const msg = el.querySelector('.latest-message');
        if (msg) msg.textContent = d.message || '';
        el.querySelector('.latest-time').textContent = d.updatedAt;

        const img = el.querySelector('.latest-avatar');
        if (img && img.tagName === 'IMG' && d.profilePicture) img.src = `/uploads/${d.profilePicture}`;
    } catch (e) {
        // silently ignore — stale banner is fine
    }
}

setInterval(refreshLatest, 10000);
