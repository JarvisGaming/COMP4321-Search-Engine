<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Google Style Search</title>
    <style>
        
        body {
            font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            margin: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 80vh; 
            background-color: #ffffff;
        }

        
        .logo {
            font-size: 80px;
            font-weight: bold;
            margin-bottom: 30px;
            user-select: none;
        }
        .logo span:nth-child(1) { color: #4285F4; } 
        .logo span:nth-child(2) { color: #EA4335; } 
        .logo span:nth-child(3) { color: #FBBC05; } 
        .logo span:nth-child(4) { color: #4285F4; } 
        .logo span:nth-child(5) { color: #34A853; } 
        .logo span:nth-child(6) { color: #EA4335; } 

        
        .search-container {
            width: 100%;
            max-width: 584px;
            position: relative;
        }

        .search-form {
            display: flex;
            align-items: center;
            padding: 5px 15px;
            border: 1px solid #dfe1e5;
            border-radius: 24px;
            transition: box-shadow 0.2s;
        }

        .search-form:hover, .search-form:focus-within {
            box-shadow: 0 1px 6px rgba(32,33,36,0.28);
            border-color: rgba(223,225,229,0);
        }

        
        input[type="text"] {
            flex: 1;
            height: 34px;
            font-size: 16px;
            border: none;
            outline: none;
            padding-left: 10px;
            background-color: transparent;
        }

        
        .button-group {
            margin-top: 25px;
            display: flex;
            gap: 12px;
        }

        button {
            background-color: #f8f9fa;
            border: 1px solid #f8f9fa;
            border-radius: 4px;
            color: #3c4043;
            font-size: 14px;
            padding: 0 16px;
            line-height: 27px;
            height: 36px;
            min-width: 54px;
            cursor: pointer;
            transition: border 0.1s, color 0.1s;
        }

        button:hover {
            box-shadow: 0 1px 1px rgba(0,0,0,0.1);
            background-image: linear-gradient(top,#f8f9fa,#f1f3f4);
            background-color: #f8f9fa;
            border: 1px solid #dadce0;
            color: #202124;
        }

        .footer-info {
            position: absolute;
            bottom: 20px;
            font-size: 13px;
            color: #70757a;
        }
    </style>
</head>
<body>

    <div class="logo">
        <span>U</span><span>S</span><span>T</span><span>G</span><span>L</span><span>E</span>
    </div>

    <div class="search-container">
        <form action="query.jsp" method="get" id="searchForm" class="search-form">
            <svg focusable="false" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="#9aa0a6">
                <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
            </svg>

            <input type="text" name="q"
                   value="<%= request.getParameter("q") != null ? request.getParameter("q") : "" %>"
                   autocomplete="off" required>

            <input type="hidden" name="exact" id="exactParam" value="false">
        </form>
    </div>

    <div class="button-group">
        <button type="button" onclick="submitWithExact(false)">Fuzzy Search</button>

        <button type="button" onclick="submitWithExact(true)">Exact Search</button>
    </div>

    <div class="footer-info">
        Server Location: Hong Kong | Time: <%= new java.util.Date() %>
    </div>

    <script>
        /**
         * Sets the hidden 'exact' field and submits the form
         * @param {boolean} isExact
         */
        function submitWithExact(isExact) {
            // Set the value of the hidden input
            document.getElementById('exactParam').value = isExact;
            // Submit the form
            document.getElementById('searchForm').submit();
        }

    </script>

</body>
</html>