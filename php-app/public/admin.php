<?php

declare(strict_types=1);

require_once __DIR__ . '/../src/functions.php';

$errors = [];
$success = false;

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $requiredFields = ['title', 'release_year', 'runtime', 'genre', 'rating', 'votes', 'overview', 'poster_url'];

    foreach ($requiredFields as $field) {
        if (!isset($_POST[$field]) || trim((string) $_POST[$field]) === '') {
            $errors[] = sprintf('Field "%s" is required.', $field);
        }
    }

    if ((float) ($_POST['rating'] ?? 0) > 10 || (float) ($_POST['rating'] ?? 0) < 0) {
        $errors[] = 'Rating must be between 0 and 10.';
    }

    if ($errors === []) {
        createMovie([
            'title' => trim((string) $_POST['title']),
            'release_year' => trim((string) $_POST['release_year']),
            'runtime' => trim((string) $_POST['runtime']),
            'genre' => trim((string) $_POST['genre']),
            'rating' => trim((string) $_POST['rating']),
            'votes' => trim((string) $_POST['votes']),
            'overview' => trim((string) $_POST['overview']),
            'poster_url' => trim((string) $_POST['poster_url']),
        ]);

        $success = true;
        $_POST = [];
    }
}

$movies = fetchMovies();
?>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Admin • AAMovies</title>
  <link rel="stylesheet" href="styles.css">
</head>
<body>
  <main class="container">
    <header class="topbar">
      <a class="logo" href="index.php">AA<span>Movies</span></a>
      <a class="button" href="index.php">Public Site</a>
    </header>

    <?php if ($success): ?>
      <p class="notice">Movie added successfully.</p>
    <?php endif; ?>

    <?php foreach ($errors as $error): ?>
      <p class="notice" style="border-color: #ff8998; background: rgba(255, 137, 152, .12);"><?= e($error) ?></p>
    <?php endforeach; ?>

    <section class="admin-layout">
      <div class="panel">
        <h2>Add Movie</h2>
        <form method="post">
          <p><input name="title" placeholder="Title" value="<?= e((string) ($_POST['title'] ?? '')) ?>"></p>
          <p><input name="release_year" type="number" placeholder="Release year" value="<?= e((string) ($_POST['release_year'] ?? '')) ?>"></p>
          <p><input name="runtime" type="number" placeholder="Runtime (minutes)" value="<?= e((string) ($_POST['runtime'] ?? '')) ?>"></p>
          <p><input name="genre" placeholder="Genre e.g. Action, Drama" value="<?= e((string) ($_POST['genre'] ?? '')) ?>"></p>
          <p><input name="rating" type="number" min="0" max="10" step="0.1" placeholder="Rating /10" value="<?= e((string) ($_POST['rating'] ?? '')) ?>"></p>
          <p><input name="votes" type="number" min="0" placeholder="Votes" value="<?= e((string) ($_POST['votes'] ?? '')) ?>"></p>
          <p><input name="poster_url" placeholder="Poster image URL" value="<?= e((string) ($_POST['poster_url'] ?? '')) ?>"></p>
          <p><textarea name="overview" rows="5" placeholder="Movie overview"><?= e((string) ($_POST['overview'] ?? '')) ?></textarea></p>
          <button type="submit">Add Movie</button>
        </form>
      </div>

      <div class="panel">
        <h2>Catalog</h2>
        <table class="table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Year</th>
              <th>Rating</th>
            </tr>
          </thead>
          <tbody>
            <?php foreach ($movies as $movie): ?>
              <tr>
                <td><a href="movie.php?id=<?= (int) $movie['id'] ?>"><?= e((string) $movie['title']) ?></a></td>
                <td><?= (int) $movie['release_year'] ?></td>
                <td><?= number_format((float) $movie['rating'], 1) ?></td>
              </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</body>
</html>
