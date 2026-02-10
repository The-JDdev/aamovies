<?php

declare(strict_types=1);

require_once __DIR__ . '/../src/functions.php';

$id = isset($_GET['id']) ? (int) $_GET['id'] : 0;
$movie = fetchMovieById($id);

if ($movie === null) {
    http_response_code(404);
    echo 'Movie not found.';
    exit;
}

$cast = fetchCastByMovieId($id);
$reviews = fetchReviewsByMovieId($id);
?>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><?= e((string) $movie['title']) ?> • AAMovies</title>
  <link rel="stylesheet" href="styles.css">
</head>
<body>
  <main class="container">
    <header class="topbar">
      <a class="logo" href="index.php">AA<span>Movies</span></a>
      <a class="button" href="index.php">← Back</a>
    </header>

    <section class="detail">
      <img class="poster card" src="<?= e((string) $movie['poster_url']) ?>" alt="<?= e((string) $movie['title']) ?> poster">

      <div class="panel">
        <h1><?= e((string) $movie['title']) ?></h1>
        <p class="meta"><?= e((string) $movie['genre']) ?> • <?= (int) $movie['release_year'] ?> • <?= (int) $movie['runtime'] ?> min</p>
        <p><?= e((string) $movie['overview']) ?></p>

        <p class="rating">★ <?= number_format((float) $movie['rating'], 1) ?>/10 from <?= formatVotes((int) $movie['votes']) ?> votes</p>

        <h3>Top Cast</h3>
        <ul>
          <?php foreach ($cast as $member): ?>
            <li><strong><?= e((string) $member['actor_name']) ?></strong> as <?= e((string) $member['role_name']) ?></li>
          <?php endforeach; ?>
        </ul>

        <h3>User Reviews</h3>
        <?php foreach ($reviews as $review): ?>
          <article class="panel" style="margin-bottom: .6rem;">
            <strong><?= e((string) $review['reviewer_name']) ?></strong> rated <strong><?= (int) $review['score'] ?>/10</strong>
            <p><?= e((string) $review['comment']) ?></p>
          </article>
        <?php endforeach; ?>
      </div>
    </section>
  </main>
</body>
</html>
