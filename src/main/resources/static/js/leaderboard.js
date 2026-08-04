async function refreshLatest() {
    try {
        const res = await fetch('/api/leaderboard/latest');
        if (res.status === 204) return; // no scores yet

        const d   = await res.json();
        const el  = document.getElementById('latestBanner');
        if (!el) return;

        el.querySelector('.latest-name').textContent   = d.username;
        el.querySelector('.latest-detail').innerHTML   =
            `gjorde precis <strong>${d.eventValue}</strong> i <span>${d.eventName}</span>`;
        el.querySelector('.latest-total').innerHTML    =
            `Total: <strong>${d.totalScore}</strong>`;
        el.querySelector('.latest-time').textContent   = d.updatedAt;

        const img = el.querySelector('.latest-avatar');
        if (img && d.profilePicture) img.src = `/uploads/${d.profilePicture}`;
    } catch (e) {
        // silently ignore — stale banner is fine
    }
}

setInterval(refreshLatest, 10000);
