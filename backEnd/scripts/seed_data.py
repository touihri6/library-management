import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import date, datetime, timedelta

AUTHORS = [
    {"firstName": "Frank", "lastName": "Herbert", "nationality": "American", "birthYear": 1920,
     "biography": "Author of the Dune saga."},
    {"firstName": "J.R.R.", "lastName": "Tolkien", "nationality": "British", "birthYear": 1892,
     "biography": "Philologist and author of The Lord of the Rings."},
    {"firstName": "Agatha", "lastName": "Christie", "nationality": "British", "birthYear": 1890,
     "biography": "The best-selling mystery writer of all time."},
    {"firstName": "Yuval Noah", "lastName": "Harari", "nationality": "Israeli", "birthYear": 1976,
     "biography": "Historian, author of Sapiens."},
    {"firstName": "Robert C.", "lastName": "Martin", "nationality": "American", "birthYear": 1952,
     "biography": "Software engineer known as Uncle Bob."},
    {"firstName": "Naguib", "lastName": "Mahfouz", "nationality": "Egyptian", "birthYear": 1911,
     "biography": "Nobel Prize in Literature 1988."},
    {"firstName": "Ursula K.", "lastName": "Le Guin", "nationality": "American", "birthYear": 1929,
     "biography": "Author of Earthsea and The Left Hand of Darkness."},
]

BOOKS = [
    {"title": "Dune", "author": "Frank Herbert", "isbn": "978-0441172719", "genre": "SCIENCE_FICTION",
     "publicationYear": 1965, "price": 9.99, "availableCopies": 4},
    {"title": "Dune Messiah", "author": "Frank Herbert", "isbn": "978-0593098233", "genre": "SCIENCE_FICTION",
     "publicationYear": 1969, "price": 10.99, "availableCopies": 2},
    {"title": "The Hobbit", "author": "J.R.R. Tolkien", "isbn": "978-0547928227", "genre": "FANTASY",
     "publicationYear": 1937, "price": 12.50, "availableCopies": 3},
    {"title": "The Fellowship of the Ring", "author": "J.R.R. Tolkien", "isbn": "978-0547928210",
     "genre": "FANTASY", "publicationYear": 1954, "price": 14.90, "availableCopies": 2},
    {"title": "Murder on the Orient Express", "author": "Agatha Christie", "isbn": "978-0062693662",
     "genre": "MYSTERY", "publicationYear": 1934, "price": 8.75, "availableCopies": 3},
    {"title": "And Then There Were None", "author": "Agatha Christie", "isbn": "978-0062073488",
     "genre": "MYSTERY", "publicationYear": 1939, "price": 9.50, "availableCopies": 2},
    {"title": "Sapiens", "author": "Yuval Noah Harari", "isbn": "978-0062316097", "genre": "HISTORY",
     "publicationYear": 2011, "price": 18.00, "availableCopies": 5},
    {"title": "Homo Deus", "author": "Yuval Noah Harari", "isbn": "978-0062464316", "genre": "NON_FICTION",
     "publicationYear": 2015, "price": 17.50, "availableCopies": 2},
    {"title": "Clean Code", "author": "Robert C. Martin", "isbn": "978-0132350884", "genre": "TECHNOLOGY",
     "publicationYear": 2008, "price": 37.90, "availableCopies": 3},
    {"title": "Clean Architecture", "author": "Robert C. Martin", "isbn": "978-0134494166",
     "genre": "TECHNOLOGY", "publicationYear": 2017, "price": 34.00, "availableCopies": 1},
    {"title": "Palace Walk", "author": "Naguib Mahfouz", "isbn": "978-0385264662", "genre": "FICTION",
     "publicationYear": 1956, "price": 15.00, "availableCopies": 2},
    {"title": "The Left Hand of Darkness", "author": "Ursula K. Le Guin", "isbn": "978-0441478125",
     "genre": "SCIENCE_FICTION", "publicationYear": 1969, "price": 11.20, "availableCopies": 2},
    {"title": "A Wizard of Earthsea", "author": "Ursula K. Le Guin", "isbn": "978-0547773742",
     "genre": "FANTASY", "publicationYear": 1968, "price": 10.40, "availableCopies": 0},
]

MEMBERS = [
    {"firstName": "Youssef", "lastName": "Member", "email": "member@library.tn", "phone": "22123456",
     "membershipType": "STANDARD", "joinDays": 400},
    {"firstName": "Amine", "lastName": "Ben Ali", "email": "amine.benali@mail.tn", "phone": "98111222",
     "membershipType": "STUDENT", "joinDays": 120},
    {"firstName": "Nour", "lastName": "Haddad", "email": "nour.haddad@mail.tn", "phone": "55333444",
     "membershipType": "PREMIUM", "joinDays": 700},
    {"firstName": "Sami", "lastName": "Trabelsi", "email": "sami.trabelsi@mail.tn", "phone": None,
     "membershipType": "STANDARD", "joinDays": 60},
    {"firstName": "Lina", "lastName": "Mansour", "email": "lina.mansour@mail.tn", "phone": "20555666",
     "membershipType": "STUDENT", "joinDays": 30},
    {"firstName": "Karim", "lastName": "Jaziri", "email": "karim.jaziri@mail.tn", "phone": "29777888",
     "membershipType": "PREMIUM", "joinDays": 900},
]

EVENTS = [
    {"title": "Dune reading club", "description": "Monthly discussion around Frank Herbert's Dune.",
     "type": "READING_CLUB", "inDays": 7, "hour": 18, "location": "Reading room", "capacity": 20},
    {"title": "Short story writing workshop", "description": "Learn to write a short story in three hours.",
     "type": "WORKSHOP", "inDays": 14, "hour": 14, "location": "Room B", "capacity": 12},
    {"title": "Clean Code in practice", "description": "Conference about readable software.",
     "type": "CONFERENCE", "inDays": 21, "hour": 17, "location": "Auditorium", "capacity": 80},
    {"title": "Meet a Tunisian author", "description": "Book signing with a local novelist.",
     "type": "BOOK_SIGNING", "inDays": 30, "hour": 16, "location": "Entrance hall", "capacity": 50},
]

REVIEWS = [
    {"isbn": "978-0441172719", "reviewerName": "Youssef", "rating": 5, "comment": "A masterpiece of science fiction."},
    {"isbn": "978-0441172719", "reviewerName": "Nour", "rating": 4, "comment": "Dense but fascinating."},
    {"isbn": "978-0547928227", "reviewerName": "Lina", "rating": 5, "comment": "Perfect adventure, read it twice."},
    {"isbn": "978-0062693662", "reviewerName": "Amine", "rating": 4, "comment": "Did not see the ending coming."},
    {"isbn": "978-0062316097", "reviewerName": "Karim", "rating": 5, "comment": "Changes how you see history."},
    {"isbn": "978-0062316097", "reviewerName": "Sami", "rating": 3, "comment": "Interesting, a bit long."},
    {"isbn": "978-0132350884", "reviewerName": "Amine", "rating": 5, "comment": "Every developer should read it."},
    {"isbn": "978-0385264662", "reviewerName": "Nour", "rating": 4, "comment": "A beautiful family saga."},
]

LOANS = [
    {"isbn": "978-0441172719", "email": "member@library.tn", "startedDaysAgo": 5, "days": 14, "returned": False},
    {"isbn": "978-0547928227", "email": "amine.benali@mail.tn", "startedDaysAgo": 10, "days": 14, "returned": False},
    {"isbn": "978-0062316097", "email": "nour.haddad@mail.tn", "startedDaysAgo": 30, "days": 14, "returned": True},
    {"isbn": "978-0132350884", "email": "karim.jaziri@mail.tn", "startedDaysAgo": 3, "days": 21, "returned": False},
    {"isbn": "978-0062693662", "email": "lina.mansour@mail.tn", "startedDaysAgo": 40, "days": 14, "returned": True},
]

USERS = [
    {"username": "amine", "email": "amine.benali@mail.tn", "firstName": "Amine", "lastName": "Ben Ali",
     "password": "amine1234", "role": "MEMBER"},
    {"username": "nour", "email": "nour.haddad@mail.tn", "firstName": "Nour", "lastName": "Haddad",
     "password": "nour1234", "role": "MEMBER"},
    {"username": "sara", "email": "sara.trabelsi@library.tn", "firstName": "Sara", "lastName": "Trabelsi",
     "password": "sara1234", "role": "LIBRARIAN"},
]


class ApiError(Exception):
    def __init__(self, status, body):
        super().__init__(f"HTTP {status}: {body}")
        self.status = status
        self.body = body


class Api:
    def __init__(self, base_url, token=None):
        self.base_url = base_url.rstrip("/")
        self.token = token

    def request(self, method, path, body=None, query=None):
        url = f"{self.base_url}{path}"
        if query:
            url += "?" + urllib.parse.urlencode(query)
        data = json.dumps(body).encode() if body is not None else None
        req = urllib.request.Request(url, data=data, method=method)
        req.add_header("Accept", "application/json")
        if data is not None:
            req.add_header("Content-Type", "application/json")
        if self.token:
            req.add_header("Authorization", f"Bearer {self.token}")
        try:
            with urllib.request.urlopen(req, timeout=30) as response:
                raw = response.read()
                return json.loads(raw) if raw else None
        except urllib.error.HTTPError as error:
            raise ApiError(error.code, error.read().decode(errors="replace")) from None

    def all(self, path, **query):
        return self.request("GET", path, query={"size": 100, **query})["content"]


def fetch_token(token_url, client_id, username, password):
    form = urllib.parse.urlencode({
        "grant_type": "password", "client_id": client_id, "username": username, "password": password,
    }).encode()
    try:
        with urllib.request.urlopen(urllib.request.Request(token_url, data=form), timeout=30) as response:
            return json.loads(response.read())["access_token"]
    except urllib.error.HTTPError as error:
        sys.exit(f"Cannot log in to Keycloak as {username}: HTTP {error.code} {error.read().decode()}")
    except urllib.error.URLError as error:
        sys.exit(f"Keycloak is unreachable at {token_url}: {error.reason}")


def wait_for(api, seconds):
    deadline = time.time() + seconds
    paths = ["/books", "/authors", "/events", "/reviews"]
    while True:
        try:
            for path in paths:
                api.request("GET", path, query={"size": 1})
            return
        except (ApiError, urllib.error.URLError, OSError) as error:
            if time.time() >= deadline:
                sys.exit(f"The services are not ready after {seconds}s: {error}")
            print("Waiting for the gateway and the services...")
            time.sleep(5)


class Report:
    def __init__(self):
        self.lines = []

    def add(self, name, created, existing):
        self.lines.append(f"  {name:<9} {created:>3} created, {existing:>3} already there")
        print(self.lines[-1])


def seed_authors(api, report):
    existing = {(a["firstName"], a["lastName"]) for a in api.all("/authors")}
    created = 0
    for author in AUTHORS:
        if (author["firstName"], author["lastName"]) not in existing:
            api.request("POST", "/authors", author)
            created += 1
    report.add("authors", created, len(AUTHORS) - created)


def seed_books(api, report):
    books = {b["isbn"]: b for b in api.all("/books")}
    created = 0
    for book in BOOKS:
        if book["isbn"] not in books:
            books[book["isbn"]] = api.request("POST", "/books", book)
            created += 1
    report.add("books", created, len(BOOKS) - created)
    return books


def seed_members(api, report):
    members = {m["email"]: m for m in api.all("/members")}
    created = 0
    for member in MEMBERS:
        if member["email"] not in members:
            body = {k: v for k, v in member.items() if k != "joinDays"}
            body["joinDate"] = (date.today() - timedelta(days=member["joinDays"])).isoformat()
            members[member["email"]] = api.request("POST", "/members", body)
            created += 1
    report.add("members", created, len(MEMBERS) - created)
    return members


def seed_events(api, report):
    existing = {e["title"] for e in api.all("/events")}
    created = 0
    for event in EVENTS:
        if event["title"] not in existing:
            when = datetime.combine(date.today() + timedelta(days=event["inDays"]), datetime.min.time())
            body = {k: v for k, v in event.items() if k not in ("inDays", "hour")}
            body["eventDate"] = when.replace(hour=event["hour"]).isoformat()
            api.request("POST", "/events", body)
            created += 1
    report.add("events", created, len(EVENTS) - created)


def seed_reviews(api, report, books):
    created = 0
    for review in REVIEWS:
        book_id = books[review["isbn"]]["id"]
        names = {r["reviewerName"] for r in api.all("/reviews", bookId=book_id)}
        if review["reviewerName"] not in names:
            api.request("POST", "/reviews", {
                "bookId": book_id, "reviewerName": review["reviewerName"],
                "rating": review["rating"], "comment": review["comment"],
            })
            created += 1
    report.add("reviews", created, len(REVIEWS) - created)


def seed_loans(api, report, books, members):
    if api.request("GET", "/loans", query={"size": 1})["totalElements"] > 0:
        report.add("loans", 0, len(LOANS))
        return
    for loan in LOANS:
        start = date.today() - timedelta(days=loan["startedDaysAgo"])
        saved = api.request("POST", "/loans", {
            "bookId": books[loan["isbn"]]["id"],
            "memberId": members[loan["email"]]["id"],
            "loanDate": start.isoformat(),
            "dueDate": (start + timedelta(days=loan["days"])).isoformat(),
        })
        if loan["returned"]:
            api.request("PATCH", f"/loans/{saved['id']}/return")
    report.add("loans", len(LOANS), 0)


def seed_users(api, report):
    created = 0
    for user in USERS:
        try:
            api.request("POST", "/users", user)
            created += 1
        except ApiError as error:
            if error.status != 409:
                raise
    report.add("users", created, len(USERS) - created)


def main():
    parser = argparse.ArgumentParser(
        description="Populate the library databases through the API gateway. Safe to run several times.")
    parser.add_argument("--gateway", default=os.getenv("GATEWAY_URL", "http://localhost:8080/api/v1"))
    parser.add_argument("--keycloak", default=os.getenv("KEYCLOAK_URL", "http://localhost:8180"))
    parser.add_argument("--realm", default="library")
    parser.add_argument("--client", default="library-front")
    parser.add_argument("--librarian", default="librarian", help="librarian username (password = username)")
    parser.add_argument("--admin", default="admin", help="admin username (password = username)")
    parser.add_argument("--wait", type=int, default=0, metavar="SECONDS",
                        help="wait up to SECONDS for the gateway and the services to be ready")
    parser.add_argument("--skip-users", action="store_true", help="do not create Keycloak accounts")
    args = parser.parse_args()

    token_url = f"{args.keycloak}/realms/{args.realm}/protocol/openid-connect/token"
    librarian = Api(args.gateway, fetch_token(token_url, args.client, args.librarian, args.librarian))
    if args.wait:
        wait_for(librarian, args.wait)

    report = Report()
    print(f"Seeding {args.gateway}")
    try:
        seed_authors(librarian, report)
        books = seed_books(librarian, report)
        members = seed_members(librarian, report)
        seed_events(librarian, report)
        seed_reviews(librarian, report, books)
        seed_loans(librarian, report, books, members)
        if not args.skip_users:
            admin = Api(args.gateway, fetch_token(token_url, args.client, args.admin, args.admin))
            seed_users(admin, report)
    except ApiError as error:
        sys.exit(f"Seeding stopped: {error}")
    except urllib.error.URLError as error:
        sys.exit(f"The gateway is unreachable at {args.gateway}: {error.reason}")
    print("Done.")


if __name__ == "__main__":
    main()
