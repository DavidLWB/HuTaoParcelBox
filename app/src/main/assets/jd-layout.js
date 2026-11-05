;(function () {
 'use strict';
 if (window.top !== window) return;
 if (location.hostname !== 'logistics-mrd.jd.com') return;
 if (window.HandyJdLayout) return;

 var lastPhase = '', timer = null, lastTabClick = 0, fixedList = null, loginWatchAttached = false;

 function phase(value) {
  if (lastPhase !== value) {
   lastPhase = value;
   console.info('__HANDYCODE_JD__:' + value);
  }
 }
 function visible(el) {
  if (!el) return false;
  var rect = el.getBoundingClientRect();
  var style = getComputedStyle(el);
  return rect.width > 0 && rect.height > 0 && style.visibility !== 'hidden' && style.display !== 'none';
 }
 function leafByText(label) {
  var nodes = document.querySelectorAll('body *');
  for (var i = 0; i < nodes.length; i++) {
   var el = nodes[i];
   if (el.children.length === 0 && (el.textContent || '').trim() === label && visible(el)) return el;
  }
  return null;
 }
 function receivedTab() {
  var titles = document.querySelectorAll('.tab-item__title');
  for (var i = 0; i < titles.length; i++) {
   var title = titles[i];
   if ((title.textContent || '').trim().indexOf('我收到的') !== 0 || !visible(title)) continue;
   return {title: title, tab: title.closest('.tab-item') || title};
  }
  var nodes = document.querySelectorAll('body *');
  for (var j = 0; j < nodes.length; j++) {
   var el = nodes[j];
   var text = (el.textContent || '').trim();
   if (el.children.length !== 0 || text.indexOf('我收到的') !== 0 || !visible(el)) continue;
   var tab = el.closest('[class*="tab"], li, button, [role="tab"]');
   if (tab) return {title: el, tab: tab};
  }
  return null;
 }
 function listRoot() {
  return document.querySelector('.search__list') || document.querySelector('[class*="search__list"]');
 }
 function emptyState() {
  return document.querySelector('.empty-container') || document.querySelector('[class*="empty-container"]');
 }
 function isLogin() {
  if (/passport|plogin/.test(location.hostname)) return true;
  var list = listRoot();
  if (list && list.querySelector('.order-card')) return false;
  if (visible(emptyState())) return false;
  if (document.querySelector('input[type="password"]')) {
   var input = document.querySelector('input[type="password"]');
   if (visible(input)) return true;
  }
  var loginButton = document.querySelector('.container-btn');
  if (loginButton && visible(loginButton)
      && (loginButton.textContent || '').indexOf('登录') !== -1) return true;
  var login = leafByText('去登录') || leafByText('登录');
  return !!login;
 }
 function loggedInOnMyPage() {
  var name = document.querySelector('.my-top__name');
  if (!name) return false;
  var text = (name.textContent || '').trim();
  return !!text && text.indexOf('登录') === -1 && text.indexOf('注册') === -1;
 }
 function watchMyLogin() {
  if (loginWatchAttached) return;
  loginWatchAttached = true;
  var jumping = false;
  var jump = function () {
   if (jumping || !loggedInOnMyPage()) return;
   jumping = true;
   location.replace('https://logistics-mrd.jd.com/v3/search');
  };
  var observer = new MutationObserver(jump);
  try { observer.observe(document.documentElement, {subtree: true, childList: true, characterData: true}); } catch (error) { }
  setInterval(jump, 1000);
  jump();
 }
 function selectReceived() {  var found = receivedTab();
  if (!found) return false;
  var cls = String(found.tab.className || '');
  var active = /active|selected|current/i.test(cls) || found.tab.getAttribute('aria-selected') === 'true'
   || !!found.tab.querySelector('[class*="active"],[class*="selected"]');
  if (active) return true;
  var now = Date.now();
  if (now - lastTabClick < 1500) return false;
  lastTabClick = now;
  try { found.tab.click(); } catch (error) { }
  try { found.title.click(); } catch (error) { }
  phase('loading');
  return false;
 }
 function ensureStyle() {
  var style = document.getElementById('hc-jd-style');
  if (style) return true;
  if (!document.head) return false;
  style = document.createElement('style');
  style.id = 'hc-jd-style';
  style.textContent =
   'html.hc-jd-mode,html.hc-jd-mode body{background:transparent!important}' +
   'html.hc-jd-mode body{overflow-y:auto!important;overflow-x:hidden!important}' +
   'html.hc-jd-mode .tab-list,html.hc-jd-mode .footer-nav{display:none!important}' +
   'html.hc-jd-mode #app,html.hc-jd-mode .main{overflow:visible!important;height:auto!important;min-height:0!important}' +
   '#hc-jd-list{position:static!important;overflow:visible!important;' +
   'box-sizing:border-box!important;padding:10px 10px 18px!important;margin:0!important;' +
   'background:var(--hc-jd-bg,#fff)!important;width:auto!important;height:auto!important;' +
   'max-width:none!important;max-height:none!important}';
  document.head.appendChild(style);
  return true;
 }
 function setBackground(color) {
  if (/^#[0-9a-f]{6}$/i.test(color || '')) {
   document.documentElement.style.setProperty('--hc-jd-bg', color);
  }
 }
 function unstyle() {
  document.documentElement.classList.remove('hc-jd-mode');
  if (fixedList) {
   fixedList.id = '';
   fixedList.style.removeProperty('position');
   fixedList.style.removeProperty('top');
   fixedList.style.removeProperty('left');
   fixedList.style.removeProperty('right');
   fixedList.style.removeProperty('bottom');
   fixedList.style.removeProperty('z-index');
   fixedList.style.removeProperty('overflow-y');
   fixedList.style.removeProperty('background');
   fixedList.style.removeProperty('padding');
   fixedList = null;
  }
 }
 function apply() {
  if (location.pathname === '/v3/my') {
   unstyle();
   watchMyLogin();
   phase('login');
   return false;
  }
  if (location.hostname !== 'logistics-mrd.jd.com' || location.pathname !== '/v3/search') {
   unstyle();
   phase('source');
   return false;
  }
  if (isLogin()) {
   unstyle();
   phase('login');
   return false;
  }
  if (!selectReceived()) return false;
  var list = listRoot();
  if (!list) {
   if (visible(emptyState())) {
    phase('empty');
    return false;
   }
   phase('loading');
   return false;
  }
  if (!ensureStyle()) {
   phase('loading');
   return false;
  }
  document.documentElement.classList.add('hc-jd-mode');
  list.id = 'hc-jd-list';
  fixedList = list;
  phase('cards');
  return true;
 }
 function queue() {
  if (timer) return;
  timer = setTimeout(function () { timer = null; apply(); }, 300);
 }
 var observer = new MutationObserver(queue);
 try { observer.observe(document.documentElement, {subtree: true, childList: true, characterData: true, attributes: true}); } catch (error) { }
 setInterval(function () { if (!document.hidden) apply(); }, 1500);
 window.addEventListener('pagehide', function () { observer.disconnect(); });
 window.HandyJdLayout = {apply: apply, setBackground: setBackground};
 window.HandyJdBackground = setBackground;
 apply();
})();
