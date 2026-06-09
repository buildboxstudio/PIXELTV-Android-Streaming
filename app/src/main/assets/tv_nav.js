/**
 * PIXELTV - TV Navigation Layer v1.6.0
 * Inject this into WebView for proper D-pad navigation on Android TV.
 * 
 * How it works:
 * - Scans all clickable/interactive elements on the page
 * - Builds a spatial map of their positions
 * - D-pad moves between elements based on direction + proximity
 * - Highlights the currently focused element
 * - OK/Enter triggers click on focused element
 */
(function() {
    'use strict';

    // Avoid double-injection
    if (window.__PIXELTV_NAV) return;
    window.__PIXELTV_NAV = true;

    var currentIndex = -1;
    var elements = [];
    var highlightEl = null;
    var scanTimeout = null;

    // CSS for highlight
    var style = document.createElement('style');
    style.id = 'pixeltv-nav-style';
    style.innerHTML = `
        .pixeltv-highlight {
            position: absolute;
            border: 3px solid #E50914;
            border-radius: 8px;
            box-shadow: 0 0 20px rgba(229, 9, 20, 0.6), inset 0 0 10px rgba(229, 9, 20, 0.15);
            pointer-events: none;
            z-index: 999999;
            transition: all 0.15s ease-out;
        }
        .pixeltv-highlight-label {
            position: absolute;
            top: -24px;
            left: 4px;
            background: #E50914;
            color: #fff;
            font-size: 10px;
            font-weight: bold;
            padding: 2px 6px;
            border-radius: 4px;
            pointer-events: none;
            white-space: nowrap;
        }
    `;
    document.head.appendChild(style);

    // Create highlight overlay element
    highlightEl = document.createElement('div');
    highlightEl.className = 'pixeltv-highlight';
    highlightEl.style.display = 'none';
    document.body.appendChild(highlightEl);

    /**
     * Scan page for all interactive/clickable elements
     */
    function scanElements() {
        var selectors = [
            'a[href]',
            'button',
            'input[type="submit"]',
            'input[type="button"]',
            '[onclick]',
            '[role="button"]',
            '[tabindex]',
            '.play-btn',
            '[class*="play"]',
            '[class*="btn"]',
            '[class*="card"]',
            '[class*="item"]',
            '[class*="movie"]',
            '[class*="film"]',
            '[class*="poster"]',
            'video',
            'iframe'
        ];

        var allEls = [];
        selectors.forEach(function(sel) {
            try {
                var found = document.querySelectorAll(sel);
                for (var i = 0; i < found.length; i++) {
                    allEls.push(found[i]);
                }
            } catch(e) {}
        });

        // Filter: must be visible and have size
        elements = [];
        var seen = new Set();

        allEls.forEach(function(el) {
            if (seen.has(el)) return;
            seen.add(el);

            var rect = el.getBoundingClientRect();
            if (rect.width < 20 || rect.height < 20) return;
            if (rect.top > window.innerHeight + 200) return;
            if (rect.bottom < -200) return;
            if (rect.left > window.innerWidth + 100) return;
            if (rect.right < -100) return;

            // Check visibility
            var style = window.getComputedStyle(el);
            if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') return;

            elements.push(el);
        });

        // Sort by position (top to bottom, left to right)
        elements.sort(function(a, b) {
            var ra = a.getBoundingClientRect();
            var rb = b.getBoundingClientRect();
            var rowDiff = Math.floor(ra.top / 60) - Math.floor(rb.top / 60);
            if (rowDiff !== 0) return rowDiff;
            return ra.left - rb.left;
        });
    }

    /**
     * Find nearest element in a direction from current
     */
    function findNearest(direction) {
        if (elements.length === 0) return -1;
        if (currentIndex < 0 || currentIndex >= elements.length) return 0;

        var current = elements[currentIndex].getBoundingClientRect();
        var cx = current.left + current.width / 2;
        var cy = current.top + current.height / 2;

        var bestIndex = -1;
        var bestScore = Infinity;

        for (var i = 0; i < elements.length; i++) {
            if (i === currentIndex) continue;

            var rect = elements[i].getBoundingClientRect();
            var ex = rect.left + rect.width / 2;
            var ey = rect.top + rect.height / 2;

            var dx = ex - cx;
            var dy = ey - cy;

            var valid = false;
            var distance = 0;

            switch (direction) {
                case 'up':
                    if (dy < -10) {
                        valid = true;
                        distance = Math.abs(dy) + Math.abs(dx) * 0.5;
                    }
                    break;
                case 'down':
                    if (dy > 10) {
                        valid = true;
                        distance = Math.abs(dy) + Math.abs(dx) * 0.5;
                    }
                    break;
                case 'left':
                    if (dx < -10) {
                        valid = true;
                        distance = Math.abs(dx) + Math.abs(dy) * 0.5;
                    }
                    break;
                case 'right':
                    if (dx > 10) {
                        valid = true;
                        distance = Math.abs(dx) + Math.abs(dy) * 0.5;
                    }
                    break;
            }

            if (valid && distance < bestScore) {
                bestScore = distance;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    /**
     * Move highlight to element at index
     */
    function focusElement(index) {
        if (index < 0 || index >= elements.length) return;

        currentIndex = index;
        var el = elements[index];
        var rect = el.getBoundingClientRect();

        // Position highlight
        highlightEl.style.display = 'block';
        highlightEl.style.left = (rect.left + window.scrollX - 4) + 'px';
        highlightEl.style.top = (rect.top + window.scrollY - 4) + 'px';
        highlightEl.style.width = (rect.width + 8) + 'px';
        highlightEl.style.height = (rect.height + 8) + 'px';

        // Scroll element into view if needed
        if (rect.top < 60) {
            window.scrollBy(0, rect.top - 80);
        } else if (rect.bottom > window.innerHeight - 60) {
            window.scrollBy(0, rect.bottom - window.innerHeight + 80);
        }

        // Trigger hover for CSS effects
        var hoverEvt = new MouseEvent('mouseover', { bubbles: true });
        el.dispatchEvent(hoverEvt);
    }

    /**
     * Click the currently focused element
     */
    function clickCurrent() {
        if (currentIndex < 0 || currentIndex >= elements.length) return;

        var el = elements[currentIndex];

        // Try multiple click methods
        el.focus();
        el.click();

        // Also dispatch mouse events for stubborn elements
        var rect = el.getBoundingClientRect();
        var cx = rect.left + rect.width / 2;
        var cy = rect.top + rect.height / 2;

        ['mousedown', 'mouseup', 'click'].forEach(function(type) {
            var evt = new MouseEvent(type, {
                bubbles: true,
                cancelable: true,
                clientX: cx,
                clientY: cy
            });
            el.dispatchEvent(evt);
        });
    }

    /**
     * Navigate in direction
     */
    window.__pixeltv_navigate = function(direction) {
        // Re-scan if elements might have changed
        scanElements();

        if (elements.length === 0) return;

        if (currentIndex < 0) {
            // First navigation — pick element closest to center
            var centerX = window.innerWidth / 2;
            var centerY = window.innerHeight / 2;
            var bestDist = Infinity;
            var bestIdx = 0;

            for (var i = 0; i < elements.length; i++) {
                var rect = elements[i].getBoundingClientRect();
                var ex = rect.left + rect.width / 2;
                var ey = rect.top + rect.height / 2;
                var dist = Math.sqrt(Math.pow(ex - centerX, 2) + Math.pow(ey - centerY, 2));
                if (dist < bestDist) {
                    bestDist = dist;
                    bestIdx = i;
                }
            }
            focusElement(bestIdx);
            return;
        }

        var next = findNearest(direction);
        if (next >= 0) {
            focusElement(next);
        } else {
            // No element found in direction — scroll instead
            switch (direction) {
                case 'up': window.scrollBy(0, -200); break;
                case 'down': window.scrollBy(0, 200); break;
                case 'left': window.scrollBy(-200, 0); break;
                case 'right': window.scrollBy(200, 0); break;
            }
            // Re-scan after scroll
            setTimeout(function() {
                scanElements();
                var next2 = findNearest(direction);
                if (next2 >= 0) focusElement(next2);
            }, 200);
        }
    };

    window.__pixeltv_click = function() {
        clickCurrent();
    };

    window.__pixeltv_hide = function() {
        highlightEl.style.display = 'none';
        currentIndex = -1;
    };

    // Initial scan
    scanElements();

    // Re-scan on DOM changes
    var observer = new MutationObserver(function() {
        clearTimeout(scanTimeout);
        scanTimeout = setTimeout(scanElements, 500);
    });
    observer.observe(document.body, { childList: true, subtree: true });

    // Re-scan on scroll
    var scrollTimer = null;
    window.addEventListener('scroll', function() {
        clearTimeout(scrollTimer);
        scrollTimer = setTimeout(function() {
            scanElements();
            if (currentIndex >= 0 && currentIndex < elements.length) {
                focusElement(currentIndex);
            }
        }, 150);
    });

    // Hide ads
    var adSelectors = ['[class*="ads"]','[id*="ads"]','[class*="popup"]','[id*="popup"]','iframe[src*="ad"]','.adsbygoogle','[class*="iklan"]','[id*="iklan"]'];
    adSelectors.forEach(function(sel) {
        try {
            document.querySelectorAll(sel).forEach(function(el) {
                if (!el.querySelector('video') && !el.closest('.player') && !el.closest('[class*="play"]')) {
                    el.style.display = 'none';
                }
            });
        } catch(e) {}
    });

    // Block popups & auto-refresh
    window.open = function() { return null; };
    document.querySelectorAll('meta[http-equiv="refresh"]').forEach(function(m) { m.remove(); });

    console.log('PIXELTV Nav: Loaded, found ' + elements.length + ' elements');
})();
