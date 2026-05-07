<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="hk.ust.cse.comp4321.project.retrieval.Retriever" %>
<%@ page import="hk.ust.cse.comp4321.project.crawl.DocumentRecord" %>
<%@ page import="org.apache.commons.lang3.tuple.Pair" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>

<!DOCTYPE html>
<html>
<head>
    <title>USTGLE Search Results</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #fff;
        }

        /* Top Bar Styling */
        .top-bar {
            display: flex;
            align-items: center;
            padding: 15px 30px;
            border-bottom: 1px solid #ebebeb;
            background-color: #fff;
            position: sticky;
            top: 0;
            z-index: 100;
            gap: 20px;
        }

        .logo {
            font-size: 24px;
            font-weight: bold;
            text-decoration: none;
            display: flex;
        }

        .logo span:nth-child(1) {
            color: #4285F4;
        }

        .logo span:nth-child(2) {
            color: #EA4335;
        }

        .logo span:nth-child(3) {
            color: #FBBC05;
        }

        .logo span:nth-child(4) {
            color: #4285F4;
        }

        .logo span:nth-child(5) {
            color: #34A853;
        }

        .logo span:nth-child(6) {
            color: #EA4335;
        }

        .search-form {
            display: flex;
            align-items: center;
            flex-grow: 1;
            max-width: 800px;
            gap: 10px;
        }

        .search-input-container {
            display: flex;
            align-items: center;
            flex-grow: 1;
            border: 1px solid #dfe1e5;
            border-radius: 24px;
            padding: 5px 15px;
        }

        .search-input {
            flex: 1;
            border: none;
            outline: none;
            font-size: 16px;
            padding: 5px;
        }

        .button-group button {
            background-color: #f8f9fa;
            border: 1px solid #f8f9fa;
            border-radius: 4px;
            color: #3c4043;
            padding: 8px 16px;
            cursor: pointer;
            font-size: 13px;
        }

        .button-group button:hover {
            border: 1px solid #dadce0;
            background: #f1f3f4;
        }

        /* Results Styling */
        .main-content {
            padding: 20px 50px;
            max-width: 1000px;
        }

        .card {
            border: 1px solid #ddd;
            padding: 25px;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .keyword-pill {
            background: #f1f3f4;
            padding: 3px 8px;
            margin-right: 5px;
            border-radius: 12px;
            font-size: 16px;
            display: inline-block;
            margin-top: 5px;
        }

        .url-link {
            color: #006621;
            text-decoration: none;
            font-size: 18px;
            word-break: break-all;
        }
    </style>
</head>
<body>

<%
    // 1. Get Parameters
    String query = request.getParameter("q");
    String exactStr = request.getParameter("exact");

    // 2. Determine boolean: If exact is "true", set true. Default/null = false.
    boolean isExact = "true".equals(exactStr);

    PriorityQueue<Pair<Double, DocumentRecord>> result = null;
    double durationSeconds = 0.0;
    int totalResults = 0;

    // 3. Execute Search
    if (query != null && !query.trim().isEmpty()) {
        long startTime = System.nanoTime();

        result = Retriever.search(query, isExact);

        long endTime = System.nanoTime();
        durationSeconds = (endTime - startTime) / 1_000_000_000.0;

        if (result != null) {
            totalResults = result.size();
        }
    }
%>

<div class="top-bar">
    <a href="index.jsp" class="logo">
        <span>U</span><span>S</span><span>T</span><span>G</span><span>L</span><span>E</span>
    </a>

    <form class="search-form" id="searchForm" action="query.jsp" method="get">
        <div class="search-input-container">
            <svg focusable="false" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="18" height="18"
                 fill="#9aa0a6">
                <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
            </svg>
            <input type="text" name="q" class="search-input"
                   value="<%= (query != null) ? query : "" %>"
                   placeholder="Search anything..." autocomplete="off"/>
        </div>

        <input type="hidden" name="exact" id="exactParam" value="<%= isExact %>">

        <div class="button-group">
            <button type="button" onclick="submitWithExact(false)">Fuzzy Search</button>
            <button type="button" onclick="submitWithExact(true)" style="font-weight: bold;">Exact Search</button>
        </div>
    </form>
</div>

<div class="main-content">

    <% if (query != null && !query.trim().isEmpty()) { %>
    <div style="color: #70757a; font-size: 1.1em; margin-bottom: 20px;">
        About <%= totalResults %> results (<%= String.format("%.3f", durationSeconds) %> seconds)
        — <strong>Mode: <%= isExact ? "Exact Phrase" : "Fuzzy" %>
    </strong>
    </div>
    <% } %>

    <%
        if (result != null && !result.isEmpty()) {
            int i = 0;
            while (!result.isEmpty() && i < 50) {
                i++;
                Pair<Double, DocumentRecord> pair = result.poll();
                DocumentRecord doc = pair.getRight();
                double score = pair.getLeft();

                // Determine Label/Color based on score
                String label = "";
                String color = "";
                if (score >= 0.8) {
                    label = "Very Relevant 🥰";
                    color = "#28a745";
                } else if (score >= 0.5) {
                    label = "Relevant 😄";
                    color = "#17a2b8";
                } else if (score >= 0.3) {
                    label = "Low Relevant 😙";
                    color = "#ffc107";
                } else if (score > 0) {
                    label = "Very Low Relevant 🤨";
                    color = "#fd7e14";
                } else {
                    score = 0.0;
                    label = "Not Relevant 😑";
                    color = "#dc3545";
                }

                // Get top 5 keywords
                List<Map.Entry<String, Long>> topKeywords = doc.bodyTermFrequencies().entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toList());
    %>

    <div class="card">
        <div>
            <span style="color: #70757a;">Result <%= i %></span><br>
            <a href="<%= doc.url() %>"
               style="font-weight: bold; color: #1a0dab; text-decoration: none; font-size: 24px;">
                <%= (doc.title() == null || doc.title().isEmpty()) ? "Untitled" : doc.title() %>
            </a>
        </div>

        <p style="margin: 5px 0;">
            <a href="<%= doc.url() %>" class="url-link"><%= doc.url() %>
            </a>
        </p>

        <p style="font-size: 18px;">
            <strong>Similarity Rate:</strong>
            <span style="color: <%= color %>; font-weight: bold;">
            <%= String.format("%.4f", score) %> (<%= label %>)
        </span>
        </p>

        <p style="font-size: 16px; color: #555;">
            Last mod: <%= doc.lastModificationTimestamp().toString().replace("T", " ") %> | Size: <%= doc.pageSize() %>
            Bytes
        </p>

        <div style="margin-top: 10px;">
            <strong>Top Keywords:</strong><br>
            <% for (Map.Entry<String, Long> entry : topKeywords) { %>
            <span class="keyword-pill"><%= entry.getKey() %> (<%= entry.getValue() %>)</span>
            <% } %>
        </div>

        <div style="font-size: 15px; margin-top: 15px; color: #555; background: #fafafa; padding: 10px; border-radius: 5px;">
            <details>
                <summary style="cursor:pointer; font-weight: bold;">View Parent & Child Links</summary>
                <div style="margin-top:10px;">
                    <strong>Parent URLs:</strong><br>
                    <%= doc.parentURLs().isEmpty() ? "None" : doc.parentURLs().stream()
                            .map(url -> "<a href='" + url + "' class='url-link' style='font-size:13px;'>" + url + "</a>")
                            .collect(Collectors.joining("<br>")) %>
                </div>
                <div style="margin-top:10px;">
                    <strong>Child URLs:</strong><br>
                    <%= doc.childURLs().isEmpty() ? "None" : doc.childURLs().stream()
                            .map(url -> "<a href='" + url + "' class='url-link' style='font-size:13px;'>" + url + "</a>")
                            .collect(Collectors.joining("<br>")) %>
                </div>
            </details>
        </div>
    </div>

    <%
        }
    } else if (query != null && !query.trim().isEmpty()) {
    %>
    <p>No results found for "<strong><%= query %>
    </strong>".</p>
    <%
        }
    %>

</div>

<script>
    /**
     * Updates the hidden 'exact' field and submits the form
     */
    function submitWithExact(val) {
        document.getElementById('exactParam').value = val;
        document.getElementById('searchForm').submit();
    }

    // Default 'Enter' key behavior to trigger Fuzzy search
    document.querySelector('.search-input').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            submitWithExact(false);
        }
    });
</script>

</body>
</html>