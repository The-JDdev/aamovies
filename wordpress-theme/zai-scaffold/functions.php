<?php
/**
 * Theme Name: Z.ai Scaffold
 * Theme URI: https://chat.z.ai
 * Author: Z.ai Team
 * Description: AI-Powered Development Theme
 * Version: 1.0.0
 * Text Domain: zai-scaffold
 */

if ( ! defined( 'ABSPATH' ) ) {
    exit; // Exit if accessed directly.
}

function zai_scaffold_scripts() {
    wp_enqueue_style( 'zai-scaffold-main', get_template_directory_uri() . '/assets/css/main.css', array(), '1.0.0' );
    wp_enqueue_style( 'zai-scaffold-style', get_stylesheet_uri() );
}
add_action( 'wp_enqueue_scripts', 'zai_scaffold_scripts' );

function zai_scaffold_setup() {
    add_theme_support( 'title-tag' );
    add_theme_support( 'custom-logo', array(
        'height'      => 100,
        'width'       => 100,
        'flex-height' => true,
        'flex-width'  => true,
    ) );
    add_theme_support( 'html5', array( 'search-form', 'comment-form', 'comment-list', 'gallery', 'caption' ) );
}
add_action( 'after_setup_theme', 'zai_scaffold_setup' );

// Note: For SVG upload support, it is recommended to use a dedicated plugin like 'Safe SVG' to ensure security.
