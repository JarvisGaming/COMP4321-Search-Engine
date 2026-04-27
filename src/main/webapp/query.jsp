<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="hk.ust.cse.comp4321.project.retrieval.SearchService" %>
<%@ page import="hk.ust.cse.comp4321.project.crawl.DocumentRecord" %>
<%@ page import="org.apache.commons.lang3.tuple.Pair" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>

<!DOCTYPE html>
<html>
<head>
    <title>Search Results</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #fff;
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
            font-size: 32px;
            font-weight: bold;
            color: #4285f4;
            margin-right: 30px;
            text-decoration: none;
        }

        .search-form {
            display: flex;
            width: 100%;
            max-width: 650px;
            position: relative;
        }

        .search-input {
            width: 100%;
            padding: 12px 20px;
            font-size: 24px;
            border: 1px solid #dfe1e5;
            border-radius: 24px;
            outline: none;
            box-shadow: 0 1px 3px rgba(32,33,36,0.1);
            transition: box-shadow 0.2s;
        }
        .search-input:focus {
            box-shadow: 0 1px 6px rgba(32,33,36,0.28);
            border-color: #dfe1e5;
        }

        .search-button {
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            font-size: 24px;
            color: #4285f4;
            cursor: pointer;
            font-weight: bold;
        }

        .main-content {
            padding: 30px 50px;
            max-width: 1000px;
        }
    </style>
</head>
<body>

    <%
        String query = request.getParameter("q");
        PriorityQueue<Pair<Double, DocumentRecord>> result = null;
        double durationSeconds = 0.0;
        int totalResults = 0;

        if (query != null && !query.trim().isEmpty()) {
            long startTime = System.nanoTime();
            result = SearchService.getQuickResult(query);
            long endTime = System.nanoTime();
            durationSeconds = (endTime - startTime) / 1_000_000_000.0;

            if (result != null) {
                totalResults = result.size();
            }
        }
    %>

    <div class="top-bar">
        <a href="index.jsp" class="logo">HKUST PowerSearch</a> <form class="search-form" action="" method="get">
            <input type="text" name="q" class="search-input"
                   value="<%= (query != null) ? query : "" %>"
                   placeholder="Search anything..." />
            <button type="submit" class="search-button"><svg focusable="false" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20" fill="#9aa0a6">
                                                                        <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"></path>
                                                                    </svg>
</button>
        </form>
    </div>

    <div class="main-content">

        <% if (query != null && !query.trim().isEmpty()) { %>
            <div style="color: #70757a; font-size: 1.4em; margin-bottom: 20px;">
            About <%= totalResults %> results (<%= String.format("%.3f", durationSeconds) %> seconds)
            </div>
        <% } %>

        <%
            if (result != null && !result.isEmpty()) {
                int i = 0;
                while (!result.isEmpty() && i < 50) {
                    i++;
                    Pair<Double, DocumentRecord> pair = result.poll();
                    DocumentRecord this_result = pair.getRight();

                    List<Map.Entry<String, Long>> topKeywords = this_result.bodyTermFrequencies().entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toList());

                    double score = pair.getLeft();
                    String label = "";
                    String color = "";

                    if (score >= 0.8) {
                        label = "Very Relevant| 🥰";
                        color = "#28a745";
                    } else if (score >= 0.5) {
                        label = "Relevant 😄";
                        color = "#17a2b8";
                    } else if (score >= 0.3) {
                        label = "Low Relevant| 😙";
                        color = "#ffc107";
                    } else if (score > 0) {
                        label = "Very Low Relevant| 🤨";
                        color = "#fd7e14";
                    } else {
                        score = 0.0;
                        label = "Not Relevant| 😑";
                        color = "#dc3545";
                    }
        %>

            <div class="card" style="border: 1px solid #ddd; padding: 25px; margin-bottom: 20px; border-radius: 8px;">
                <div class="title">
                    Page <%= i %>:
                    <a href="<%= this_result.url() %>" style="font-weight: bold; color: #1a0dab; text-decoration: none; font-size: 26px;">
                        <%= (this_result.title() == null || this_result.title().isEmpty()) ? "Untitled" : this_result.title() %>
                    </a>
                </div>

                <p style="margin: 5px 0;">
                    <a href="<%= this_result.url() %>"  style="color: #006621; text-decoration: none; font-size: 21px;">
                        <%= this_result.url() %>
                    </a>
                </p>

                <p style="font-weight: bold; font-size: 21px;">
                    Similarity Rate:
                    <span style="color: <%= color %>;">
                        <%= String.format("%.4f", score) %> (<%= label.replace("|", ")") %>
                    </span>
                </p>

                <p style="font-size: 21px; color: #555;">
                    Last mod. date: <%= this_result.lastModificationTimestamp().toString().replace("T", " ") %> | Size: <%= this_result.pageSize() %> Bytes
                </p>

                <div class="keywords" style="font-size: 21px;">
                    <strong>Top Keywords:</strong>
                    <% for (Map.Entry<String, Long> entry : topKeywords) { %>
                        <span style="background:#f1f3f4; padding:3px 8px; margin-right:5px; border-radius:12px; font-size: 18px; display: inline-block; margin-top: 5px;">
                            <%= entry.getKey() %> (<%= entry.getValue() %>)
                        </span>
                    <% } %>
                </div>

                <div style="font-size: 19px; margin-top: 15px; color: #555;">
                    <div>
                        <strong>Parent URLs:</strong><br>
                        <%= this_result.parentURLs().stream()
                            .map(url -> "<a href='" + url + "'  style='color: #006621; text-decoration: none;'>" + url + "</a>")
                            .collect(Collectors.joining("<br>"))
                        %>
                    </div>

                    <div style="margin-top: 10px;">
                        <strong>Child URLs:</strong><br>
                        <%= this_result.childURLs().stream()
                            .map(url -> "<a href='" + url + "'  style='color: #006621; text-decoration: none;'>" + url + "</a>")
                            .collect(Collectors.joining("<br>"))
                        %>
                    </div>
                </div>
            </div>

        <%
                }
            } else if (query != null && !query.trim().isEmpty()) {
        %>
            <p>No results found for "<strong><%= query %></strong>".</p>
        <%
            }
        %>
    </div>
</body>
</html>