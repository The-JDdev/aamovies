<!doctype html>
<html <?php language_attributes(); ?> class="antialiased">
<head>
    <meta charset="<?php bloginfo( 'charset' ); ?>">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="profile" href="https://gmpg.org/xfn/11">

    <!-- Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@100..900&display=swap" rel="stylesheet">

    <style>
        :root {
            --font-geist-sans: "Inter", sans-serif;
            --font-geist-mono: "monospace";
        }
    </style>
    <?php wp_head(); ?>
</head>
<body <?php body_class('bg-background text-foreground'); ?>>
