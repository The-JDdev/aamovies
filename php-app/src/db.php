<?php

declare(strict_types=1);

function db(): PDO
{
    static $pdo = null;

    if ($pdo instanceof PDO) {
        return $pdo;
    }

    $dbDir = __DIR__ . '/../data';
    if (!is_dir($dbDir)) {
        mkdir($dbDir, 0777, true);
    }

    $dbPath = $dbDir . '/movies.db';
    $pdo = new PDO('sqlite:' . $dbPath);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);

    initializeSchema($pdo);
    seedDatabase($pdo);

    return $pdo;
}

function initializeSchema(PDO $pdo): void
{
    $pdo->exec(
        'CREATE TABLE IF NOT EXISTS movies (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            release_year INTEGER NOT NULL,
            runtime INTEGER NOT NULL,
            genre TEXT NOT NULL,
            rating REAL NOT NULL DEFAULT 0,
            votes INTEGER NOT NULL DEFAULT 0,
            overview TEXT NOT NULL,
            poster_url TEXT,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        )'
    );

    $pdo->exec(
        'CREATE TABLE IF NOT EXISTS casts (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            movie_id INTEGER NOT NULL,
            actor_name TEXT NOT NULL,
            role_name TEXT NOT NULL,
            FOREIGN KEY(movie_id) REFERENCES movies(id)
        )'
    );

    $pdo->exec(
        'CREATE TABLE IF NOT EXISTS reviews (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            movie_id INTEGER NOT NULL,
            reviewer_name TEXT NOT NULL,
            score INTEGER NOT NULL,
            comment TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY(movie_id) REFERENCES movies(id)
        )'
    );
}

function seedDatabase(PDO $pdo): void
{
    $exists = (int) $pdo->query('SELECT COUNT(*) FROM movies')->fetchColumn();
    if ($exists > 0) {
        return;
    }

    $movies = [
        [
            'title' => 'The Midnight Orbit',
            'release_year' => 2023,
            'runtime' => 128,
            'genre' => 'Sci-Fi, Thriller',
            'rating' => 8.3,
            'votes' => 15420,
            'overview' => 'A rescue crew travels beyond the edge of mapped space to recover a missing station before solar storms erase all communication.',
            'poster_url' => 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
            'cast' => [
                ['Mina Patel', 'Captain Aria Sol'],
                ['Liam Ortega', 'Navigator Voss'],
                ['Nora Kline', 'Chief Engineer Tali']
            ],
            'reviews' => [
                ['Jonas', 9, 'Sharp pacing and gorgeous visuals.'],
                ['Priya', 8, 'A little dense, but very rewarding sci-fi.']
            ]
        ],
        [
            'title' => 'Glass Streets',
            'release_year' => 2021,
            'runtime' => 112,
            'genre' => 'Crime, Drama',
            'rating' => 7.9,
            'votes' => 9320,
            'overview' => 'An idealistic journalist uncovers corruption tied to a string of impossible robberies in a city that never sleeps.',
            'poster_url' => 'https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=600&q=80',
            'cast' => [
                ['Elena Park', 'Mara Quinn'],
                ['David Mensah', 'Detective Cole'],
                ['Theo Barnes', 'Rook']
            ],
            'reviews' => [
                ['Ari', 8, 'Strong characters and a great ending.'],
                ['Sam', 7, 'Stylish noir vibes all the way through.']
            ]
        ],
        [
            'title' => 'Summer of Echoes',
            'release_year' => 2024,
            'runtime' => 104,
            'genre' => 'Romance, Comedy',
            'rating' => 7.4,
            'votes' => 6110,
            'overview' => 'Two rival podcasters are forced to host a travel show together and discover that their best stories are still unwritten.',
            'poster_url' => 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?auto=format&fit=crop&w=600&q=80',
            'cast' => [
                ['Noah Reed', 'Ben Carter'],
                ['Ava Lin', 'Sophie Hale'],
                ['Carlos Vega', 'Marco']
            ],
            'reviews' => [
                ['Tina', 7, 'Cozy, funny, and surprisingly emotional.'],
                ['Khalid', 8, 'Great chemistry between the leads.']
            ]
        ]
    ];

    $movieStmt = $pdo->prepare(
        'INSERT INTO movies (title, release_year, runtime, genre, rating, votes, overview, poster_url)
         VALUES (:title, :release_year, :runtime, :genre, :rating, :votes, :overview, :poster_url)'
    );

    $castStmt = $pdo->prepare(
        'INSERT INTO casts (movie_id, actor_name, role_name)
         VALUES (:movie_id, :actor_name, :role_name)'
    );

    $reviewStmt = $pdo->prepare(
        'INSERT INTO reviews (movie_id, reviewer_name, score, comment)
         VALUES (:movie_id, :reviewer_name, :score, :comment)'
    );

    foreach ($movies as $movie) {
        $movieStmt->execute([
            ':title' => $movie['title'],
            ':release_year' => $movie['release_year'],
            ':runtime' => $movie['runtime'],
            ':genre' => $movie['genre'],
            ':rating' => $movie['rating'],
            ':votes' => $movie['votes'],
            ':overview' => $movie['overview'],
            ':poster_url' => $movie['poster_url'],
        ]);

        $movieId = (int) $pdo->lastInsertId();

        foreach ($movie['cast'] as [$actor, $role]) {
            $castStmt->execute([
                ':movie_id' => $movieId,
                ':actor_name' => $actor,
                ':role_name' => $role,
            ]);
        }

        foreach ($movie['reviews'] as [$reviewer, $score, $comment]) {
            $reviewStmt->execute([
                ':movie_id' => $movieId,
                ':reviewer_name' => $reviewer,
                ':score' => $score,
                ':comment' => $comment,
            ]);
        }
    }
}
