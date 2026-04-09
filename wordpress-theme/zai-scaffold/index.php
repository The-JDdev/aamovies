<?php get_header(); ?>

<div class="container mx-auto p-4 min-h-screen">
    <?php
    if ( have_posts() ) {
        while ( have_posts() ) {
            the_post();
            ?>
            <article <?php post_class('mb-8 prose dark:prose-invert max-w-none'); ?>>
                <h1 class="text-3xl font-bold mb-4"><a href="<?php the_permalink(); ?>"><?php the_title(); ?></a></h1>
                <div class="content">
                    <?php the_content(); ?>
                </div>
            </article>
            <?php
        }
    }
    ?>
</div>

<?php get_footer(); ?>
