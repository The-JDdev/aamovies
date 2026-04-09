<?php get_header(); ?>

<div class="flex flex-col items-center justify-center min-h-screen gap-8 p-4">
    <div class="relative w-24 h-24 md:w-32 md:h-32">
        <?php
        $custom_logo_id = get_theme_mod( 'custom_logo' );
        $logo = wp_get_attachment_image_src( $custom_logo_id , 'full' );

        if ( has_custom_logo() ) {
            echo '<img src="' . esc_url( $logo[0] ) . '" alt="' . get_bloginfo( 'name' ) . '" class="w-full h-full object-contain">';
        } else {
            // Fallback to the theme asset logo
            echo '<img src="' . get_template_directory_uri() . '/assets/img/logo.svg" alt="Z.ai Logo" class="w-full h-full object-contain">';
        }
        ?>
    </div>
    <div class="prose dark:prose-invert">
        <?php
        if ( have_posts() ) {
            while ( have_posts() ) {
                the_post();
                the_content();
            }
        }
        ?>
    </div>
</div>

<?php get_footer(); ?>
