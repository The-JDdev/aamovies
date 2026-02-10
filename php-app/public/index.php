<?php

declare(strict_types=1);

require_once __DIR__ . '/../src/functions.php';

$query = $_GET['q'] ?? null;
$movies = fetchMovies(is_string($query) ? $query : null);
?>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>AAMovies • Discover Movies</title>
  <link rel="stylesheet" href="styles.css">
</head>
<body>
  <main class="container">
    <header class="topbar">
      <a class="logo" href="index.php">AA<span>Movies</span></a>
      <form class="search-form" method="get">
        <input name="q" placeholder="Search title or genre..." value="<?= e((string) $query) ?>">
        <button type="submit">Search</button>
      </form>
      <a class="button" href="admin.php">Admin</a>
    </header>

    <section>
      <h1>Trending & Top Rated</h1>
      <p class="meta">A lightweight TMDB/IMDb-style movie directory built with PHP + SQLite.</p>
    </section>

    <section class="grid">
      <?php foreach ($movies as $movie): ?>
        <article class="card">
          <a href="movie.php?id=<?= (int) $movie['id'] ?>">
            <img class="poster" src="<?= e((string) $movie['poster_url']) ?>" alt="<?= e((string) $movie['title']) ?> poster">
          </a>
          <div class="card-content">
            <h2><a href="movie.php?id=<?= (int) $movie['id'] ?>"><?= e((string) $movie['title']) ?></a></h2>
            <div class="meta"><?= e((string) $movie['genre']) ?> • <?= (int) $movie['release_year'] ?> • <?= (int) $movie['runtime'] ?> min</div>
            <span class="rating">★ <?= number_format((float) $movie['rating'], 1) ?> / 10 (<?= formatVotes((int) $movie['votes']) ?> votes)</span>
          </div>
        </article>
      <?php endforeach; ?>
    </section>
  </main>
</body>
</html>
