const voteUrl = 'http://www.xtremetop100.com/in.php?site=1132512425';

function ajaxPost(url, data) {
  $.ajax({
    type: 'POST',
    url,
    data,
    cache: false,
    success: msg => alert(msg)
  });
}

function createCookie(name, value, mins) {
  let expires = '';
  if (mins) {
    const date = new Date();
    date.setTime(date.getTime() + mins * 60 * 1000);
    expires = '; expires=' + date.toUTCString();
  }
  document.cookie = `${name}=${value}${expires}; path=/`;
}

function readCookie(name) {
  const nameEQ = name + '=';
  const cookies = document.cookie.split(';');
  for (let c of cookies) {
    c = c.trim();
    if (c.startsWith(nameEQ)) return c.substring(nameEQ.length);
  }
  return null;
}

function eraseCookie(name) {
  createCookie(name, '', -1);
}

function checkVote() {
  const voteBg = document.getElementById('votebg');
  if (!readCookie('ghlastvote')) {
    if (!readCookie('ghmmorpgtoplist')) mmorpgtoplistF();
    else if (!readCookie('ghtop100arena')) top100arenaF();
    else if (!readCookie('ghtopofgames')) topofgamesF();

    const isIE = navigator.appName.includes('Microsoft');
    voteBg.style.display = isIE ? 'block' : 'table';
  } else {
    voteBg.style.display = 'none';
    loadLocalClient();
  }
}

function showVote() {
  if (!readCookie('ghiff')) {
    document.getElementById('trans').style.display = 'block';
    const popup = document.getElementById('popup');
    popup.style.display = 'block';
    popup.innerHTML = `
      Earn $5 million for every Facebook friend you invite to play GodzHell<br/>
      <form id="iffform" style="border: 2px solid;">
        Player Name: <input type="text" id="iffusername" /><br/>
        <a href="javascript:void(0)" onclick="iff();">Invite friends</a>
      </form><br/>
      <a href="javascript:void(0)" onclick="dontiff();">or I just want to play</a>
    `;
  } else {
    dontiff();
  }
}

function iff() {
  const username = document.getElementById('iffusername').value;
  if (!username) {
    alert('Please enter your player name');
    return;
  }

  FB.ui({
    method: 'apprequests',
    message: 'Join me on GodzHell.com and battle to rule the world!',
    title: 'Invite your friends'
  }, response => {
    if (response && response.request_ids) {
      ajaxPost('makeapprequest.html', `requestsize=${response.request_ids.length}&un=${username}`);
      createCookie('ghiff', Date.now(), 10080); // 1 week
      dontiff();
    }
  });
}

function dontiff() {
  document.getElementById('popup').style.display = 'none';
  document.getElementById('trans').style.display = 'none';
  loadClient();
  window.open('./downloads.htm', '', 'width=900,height=600,menubar=yes,status=yes,scrollbars=yes,resizable=yes');
}

function goVote() {
  const now = Date.now();
  if (!readCookie('ghmmorpgtoplist')) {
    createCookie('ghmmorpgtoplist', now, 720);
  } else if (!readCookie('ghtop100arena')) {
    createCookie('ghtop100arena', now, 1440);
  } else if (!readCookie('ghtopofgames')) {
    createCookie('ghtopofgames', now, 720);
    createCookie('ghlastvote', now, 480);
  }

  document.getElementById('votebg').style.display = 'none';
  loadLocalClient();
}

function dontVote() {
  document.getElementById('votebg').style.display = 'none';
  loadLocalClient();
  launchAd();
}

function launchAd() {
  window.open('http://www.godzhell.com/novote.htm', '', 'width=900,height=600,menubar=yes,status=yes,scrollbars=yes,resizable=yes');
}

function loadClient() {
  document.getElementById('client').innerHTML = `
    <div valign="top">
      <applet class="client" code="client.class" name="GodzHell.com" width="765" height="503"
        archive="http://godzhell.webatu.com/clients/current.jar" id="GodzHell.com">
        <param name="java_arguments" value="-client">
        <param name="java_arguments" value="-Xmx512m">
        <param name="separate_jvm" value="true">
        <param name="image" value="images/ghloading.html">
        <param name="location" value="na">
      </applet>
    </div>
  `;
}

function loadLocalClient() {
  loadClient();
}

function loadPlayNow() {
  document.getElementById('client').innerHTML = `
    <img src="http://www.godzhell.com/images/playnowNA.gif" width="765" height="503" onclick="checkVote();" />
  `;
}

function contact() {
  const isIE = navigator.appName.includes('Microsoft');
  document.getElementById('votebg').style.display = isIE ? 'block' : 'table';
}

function top100arenaF() {
  document.getElementById('votelink').innerHTML = `
    Receive $100,000gh for voting!<br />
    Enter your player name below and click the vote image.<br />
    <form>
      <input type="text" name="usernamev" id="usernamev" size="25" /><br />
      <img src="http://www.topmmolist.com/images/topmmolist.gif" width="88" height="43" border="0" onclick="vi();" />
    </form><br />
    You can get the $100,000gh once every day. So keep voting!
  `;
}

function mmorpgtoplistF() {
  document.getElementById('votelink').innerHTML = `
    <a onclick="goVote();" href="http://www.mmorpgtoplist.com/in.php?site=14858" target="_blank">
      Click here to vote<br />
      <img src="../www.godzhell.com/images/mmorpgtoplist.jpg" />
    </a>
  `;
}

function topofgamesF() {
  document.getElementById('votelink').innerHTML = `
    <a onclick="goVote();" href="http://www.mmorpgtoplist.com/in.php?site=14858" target="_blank">
      Click here to vote
    </a>
  `;
}

function vi() {
  let username = encodeURIComponent(document.getElementById('usernamev').value).replace(/%20/g, '^');
  window.open(`http://runescape.top100arena.com/in.asp?id=23574&incentive=${username}`);
  goVote();
}
