<?php
require_once("../render.inc.php");
require("../render_game.inc.php");

$clanwars = json_decode(file_get_contents('http://woop.ac:81/ActionFPS-PHP-Iterator/api/clanwars.php?count=50'), true);
?>
<div id="games">
    <br /><br />
    <?php foreach($clanwars as $war) render_war($war); ?>
</div>
<?php echo $foot;