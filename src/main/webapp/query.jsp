<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="hk.ust.cse.comp4321.project.retrieval.Retriever" %>
<%@ page import="hk.ust.cse.comp4321.project.crawl.DocumentRecord" %>
<%@ page import="org.apache.commons.lang3.tuple.Pair" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>

<!DOCTYPE html>
<html>
<head>
    <title>HKUST PowerSearch - Results</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #ffffff;
            color: #202124;
        }

        .top-bar {
            display: flex;
            align-items: center;
            padding: 20px 30px;
            border-bottom: 1px solid #ebebeb;
            background-color: #fff;
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .logo {
            font-size: 28px;
            font-weight: bold;
            color: #4285f4;
            margin-right: 30px;
            text-decoration: none;
        }

        .search-form {
            display: flex;
            align-items: center;
            width: 100%;
            max-width: 800px;
            gap: 15px;
        }

        .search-input {
            flex: 1;
            padding: 12px 20px;
            font-size: 16px;
            border: 1px solid #dfe1e5;
            border-radius: 24px;
            outline: none;
            box-shadow: 0 1px 3px rgba(32,33,36,0.1);
        }

        .search-input:focus {
            box-shadow: 0 1px 6px rgba(32,33,36,0.28);
        }

        .button-group {
            display: flex;
            gap: 8px;
        }

        .button-group button {
            padding: 10px 16px;
            font-size: 14px;
            border: 1px solid #f8f9fa;
            border-radius: 4px;
            background-color: #f8f9fa;
            color: #3c4043;
            cursor: pointer;
            transition: border 0.2s;
        }

        .button-group button:hover {
            border: 1px solid #dadce0;
            background-color: #f1f3f4;
        }

        .main-content {
            padding: 20px 50px 50px 160px; /* Aligned like Google results */
            max-width: 800px;
        }

        .stats-label {
            color: #70757a;
            font-size: 14px;
            margin-bottom: 20px;
        }

        .result-card {
            margin-bottom: 30px;
        }

        .result-url {
            font-size: 14px;
            color: #202124;
            margin-bottom: 4px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            display: block;
        }

        .result-title {
            font-size: 20px;
            color: #1a0dab;
            text-decoration: none;
            display: inline-block;
            margin-bottom: 4px;
        }

        .result-title:hover {
            text-decoration: underline;
        }

        .result-meta {
            font-size: 13px;
            color: #4d5156;
            line-height: 1.5;
        }

        .similarity-tag {
            font-weight: bold;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 12px;
        }

        .keyword-pill {
            background: #f1f3f4;
            padding: 4px 10px;
            margin-right: 6px;
            border-radius: 16px;
            font-size: 12px;
            display: inline-block;
            margin-top: 8px;
        }
    </style>
</head>
<body>

<%
    // Logic Phase
    String query = request.getParameter("q");
    String exactParam = request.getParameter("exact");
    boolean isExact = "true".equals(exactParam);

    PriorityQueue<Pair<Double, DocumentRecord>> result = null;
    double durationSeconds = 0.0;
    int totalResults = 0;

    if (query != null && !query.trim().isEmpty()) {
        long startTime = System.nanoTime();

        // Calling your Retriever logic
        result = Retriever.search(query, isExact);

        long endTime = System.nanoTime();
        durationSeconds = (endTime - startTime) / 1_000_000_000.0;

        if (result != null) {
            totalResults = result.size();
        }
    }
%>

<div class="top-bar">
    <a href="index.jsp" class="logo">HKUST</a>

    <form class="search-form" action="" method="get">
        <input type="text" name="q" class="search-input"
               value="<%= (query != null) ? query : "" %>"
               placeholder="Search HKUST..." required />

        <div class="button-group">
            <button type="submit" name="exact" value="false">Search</button>
            <button type="submit" name="exact" value="true">Exact Match</button>
        </div>
    </form>
</div>

<div class="main-content">

    <% if (query != null && !query.trim().isEmpty()) { %>
        <div class="stats-label">
            About <%= totalResults %> results (<%= String.format("%.3f", durationSeconds) %> seconds)
            <% if (isExact) { %> <strong>[Exact Phrase Mode]</strong> <% } %>
        </div>

        <%
            if (result != null && !result.isEmpty()) {
                int count = 0;
                while (!result.isEmpty() && count < 50) {
                    count++;
                    Pair<Double, DocumentRecord> pair = result.poll();
                    DocumentRecord doc = pair.getRight();
                    double score = pair.getLeft();

                    // Formatting Label & Color
                    String label;
                    String color;
                    if (score >= 0.8) { label = "Highly Relevant"; color = "#1e7e34"; }
                    else if (score >= 0.5) { label = "Relevant"; color = "#0c5460"; }
                    else if (score >= 0.2) { label = "Potential Match"; color = "#856404"; }
                    else { label = "Low Relevance"; color = "#721c24"; }

                    List<Map.Entry<String, Long>> topKeywords = doc.bodyTermFrequencies().entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toList());
        %>

        <div class="result-card">
            <span class="result-url"><%= doc.url() %></span>
            <a href="<%= doc.url() %>" class="result-title">
                <%= (doc.title() == null || doc.title().isEmpty()) ? "Untitled Page" : doc.title() %>
            </a>

            <div class="result-meta">
                <span class="similarity-tag" style="background-color: <%= color %>22; color: <%= color %>;">
                    Score: <%= String.format("%.4f", score) %> — <%= label %>
                </span>
                <br>
                Modified: <%= doc.lastModificationTimestamp().toString().replace("T", " ") %> |
                Size: <%= doc.pageSize() %> Bytes
            </div>

            <div class="keywords">
                <% for (Map.Entry<String, Long> entry : topKeywords) { %>
                    <span class="keyword-pill"><%= entry.getKey() %> (<%= entry.getValue() %>)</span>
                <% } %>
            </div>
        </div>

        <%
                }
            } else {
        %>
            <p>Your search - <strong><%= query %></strong> - did not match any documents.</p>
            <ul>
                <li>Make sure all words are spelled correctly.</li>
                <li>Try different keywords.</li>
                <li>Try more general keywords.</li>
            </ul>
        <%
            }
        } else { %>
            <h3>Please enter a query to begin searching.</h3>
        <% } %>

</div>

</body>
</html>