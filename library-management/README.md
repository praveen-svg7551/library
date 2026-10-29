# Library Management System (Java + Vercel)

Mini project for the ExcelR Full Stack Java internship (APSCHE).
Concepts used: OOP (encapsulation, custom exception), Collections (LinkedHashMap, ArrayList),
File I/O persistence, REST API with the JDK HttpServer, HTML/CSS/JS frontend.

## Structure
- `backend/`  Java 17 API (no external libraries) + Dockerfile
- `public/`   Frontend, served by Vercel
- `vercel.json` Proxies /api/* to the Java backend

## Run locally
    cd backend
    mkdir out && javac -d out $(find src -name "*.java")
    java -cp out library.Main          # API on http://localhost:8080

## Deploy
1. Push this folder to GitHub.
2. Render -> New Web Service -> select the repo -> Root Directory `backend` -> Runtime Docker -> Free plan.
   Copy the URL, e.g. https://library-api.onrender.com
3. Edit `vercel.json` and replace YOUR-APP-NAME.onrender.com with that host. Commit and push.
4. Vercel -> Add New Project -> import the repo -> Framework "Other" -> Output Directory `public` -> Deploy.

Note: Render's free tier sleeps when idle (first request takes ~30-50 s) and its disk is temporary,
so data resets on restart. For permanent data, replace file storage with MySQL via JDBC.

## API
GET /api/books | GET /api/members | POST /api/books (title, author, copies) | POST /api/members (name)
POST /api/issue (memberId, bookId) | POST /api/return (memberId, bookId) | POST /api/delete (bookId)
