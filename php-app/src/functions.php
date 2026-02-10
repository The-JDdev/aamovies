<?php

declare(strict_types=1);

require_once __DIR__ . '/db.php';

function e(string $value): string
{
    return htmlspecialchars($value, ENT_QUOTES, 'UTF-8');
}

function fetchMovies(?string $query = null): array
{
    $pdo = db();

    if ($query !== null && trim($query) !== '') {
        $stmt = $pdo->prepare(
            'SELECT * FROM movies
             WHERE title LIKE :query OR genre LIKE :query
             ORDER BY rating DESC, release_year DESC'
        );
        $stmt->execute([':query' => '%' . trim($query) . '%']);

        return $stmt->fetchAll();
    }

    return $pdo
        ->query('SELECT * FROM movies ORDER BY rating DESC, votes DESC, release_year DESC')
        ->fetchAll();
}

function fetchMovieById(int $id): ?array
{
    $pdo = db();
    $stmt = $pdo->prepare('SELECT * FROM movies WHERE id = :id');
    $stmt->execute([':id' => $id]);
    $movie = $stmt->fetch();

    return $movie ?: null;
}

function fetchCastByMovieId(int $movieId): array
{
    $stmt = db()->prepare('SELECT actor_name, role_name FROM casts WHERE movie_id = :movie_id');
    $stmt->execute([':movie_id' => $movieId]);

    return $stmt->fetchAll();
}

function fetchReviewsByMovieId(int $movieId): array
{
    $stmt = db()->prepare(
        'SELECT reviewer_name, score, comment, created_at
         FROM reviews
         WHERE movie_id = :movie_id
         ORDER BY created_at DESC'
    );
    $stmt->execute([':movie_id' => $movieId]);

    return $stmt->fetchAll();
}

function createMovie(array $data): void
{
    $stmt = db()->prepare(
        'INSERT INTO movies (title, release_year, runtime, genre, rating, votes, overview, poster_url)
         VALUES (:title, :release_year, :runtime, :genre, :rating, :votes, :overview, :poster_url)'
    );

    $stmt->execute([
        ':title' => $data['title'],
        ':release_year' => (int) $data['release_year'],
        ':runtime' => (int) $data['runtime'],
        ':genre' => $data['genre'],
        ':rating' => (float) $data['rating'],
        ':votes' => (int) $data['votes'],
        ':overview' => $data['overview'],
        ':poster_url' => $data['poster_url'],
    ]);
}

function formatVotes(int $votes): string
{
    return number_format($votes);
}
