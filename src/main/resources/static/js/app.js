document.addEventListener('DOMContentLoaded', function () {
  var path = window.location.pathname || '/';
  // If multiple navbars exist, prefer the fragment one and remove others
  var navbars = Array.prototype.slice.call(document.querySelectorAll('nav.navbar'));
  var fragmentNav = document.querySelector('nav.navbar[data-fragment="navbar"]');
  if (fragmentNav && navbars.length > 1) {
    navbars.forEach(function (n) { if (n !== fragmentNav) { n.parentNode && n.parentNode.removeChild(n); } });
  }

  var links = document.querySelectorAll('nav.navbar a.nav-link');
  links.forEach(function (a) {
    try {
      var u = new URL(a.getAttribute('href'), window.location.origin);
      var hrefPath = u.pathname;
      if ((path === '/' || path.startsWith('/projects')) && hrefPath === '/projects') {
        a.classList.add('active');
      }
      if (path.startsWith('/stats') && hrefPath === '/stats') {
        a.classList.add('active');
      }
    } catch (e) {
      // ignore
    }
  });
});
