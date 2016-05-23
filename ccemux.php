<select name="kat" style="FONT-SIZE: 10px; WIDTH: 220px; COLOR: #000000; FONT-FAMILY: Verdana, Helvetica; HEIGHT: 16px; BACKGROUND-COLOR: #ffffff">
  <option value="">-- Bitte ausw?hlen --</option>
  <option<?php if ($row[kat] == "1") echo " selected"?> value="1">Auto und Verkehr</option>
  <option<?php if ($row[kat] == "2") echo " selected"?> value="2">Bekanntschaften und Kontakt</option>
  <option<?php if ($row[kat] == "3") echo " selected"?> value="3">Bildung und Karriere</option>
  <option<?php if ($row[kat] == "4") echo " selected"?> value="4">Business und Marketing</option>
  <option<?php if ($row[kat] == "5") echo " selected"?> value="5">Computer/Hard- und Software</option>
  <option<?php if ($row[kat] == "6") echo " selected"?> value="6">Dienstleistungen</option>
  <option<?php if ($row[kat] == "7") echo " selected"?> value="7">Erotik</option>
  <option<?php if ($row[kat] == "8") echo " selected"?> value="8">Essen und Trinken</option>
  <option<?php if ($row[kat] == "9") echo " selected"?> value="9">Finanzen</option>
  <option<?php if ($row[kat] == "10") echo " selected"?> value="10">Gesellschaft und Politik</option>
  <option<?php if ($row[kat] == "11") echo " selected"?> value="11">Gesundheit und Fitness</option>
  <option<?php if ($row[kat] == "12") echo " selected"?> value="12">Handel und Wirtschaft</option>
  <option<?php if ($row[kat] == "13") echo " selected"?> value="13">Hobby und Freizeit</option>
  <option<?php if ($row[kat] == "14") echo " selected"?> value="14">Geld verdienen</option>
  <option<?php if ($row[kat] == "15") echo " selected"?> value="15">Internet</option>
  <option<?php if ($row[kat] == "16") echo " selected"?> value="16">Kostenloses und Schn?ppchen</option>
  <option<?php if ($row[kat] == "17") echo " selected"?> value="17">Kunst und Kultur</option>
  <option<?php if ($row[kat] == "18") echo " selected"?> value="18">Mode und Kosmetik</option>
  <option<?php if ($row[kat] == "19") echo " selected"?> value="19">Musik und Unterhaltung</option>
  <option<?php if ($row[kat] == "20") echo " selected"?> value="20">Nachrichten und Medien</option>
  <option<?php if ($row[kat] == "21") echo " selected"?> value="21">Natur und Umwelt</option>
  <option<?php if ($row[kat] == "22") echo " selected"?> value="22">Online-Shopping</option>
  <option<?php if ($row[kat] == "23") echo " selected"?> value="23">Private Homepage</option>
  <option<?php if ($row[kat] == "24") echo " selected"?> value="24">Regionale Informationen</option>
  <option<?php if ($row[kat] == "25") echo " selected"?> value="25">Reisen und Urlaub</option>
  <option<?php if ($row[kat] == "26") echo " selected"?> value="26">Spiele</option>
  <option<?php if ($row[kat] == "27") echo " selected"?> value="27">Sport</option>
  <option<?php if ($row[kat] == "28") echo " selected"?> value="28">Telekommunikation und Mobilfunk</option>
  <option<?php if ($row[kat] == "29") echo " selected"?> value="29">Verzeichnisse und Referenzen</option>
  <option<?php if ($row[kat] == "30") echo " selected"?> value="30">Wissenschaft und Technik</option>
  <option<?php if ($row[kat] == "31") echo " selected"?> value="31">Sonstiges</option>
</select>
<?php
/**
 * Add magic quotes to $_GET, $_POST, $_COOKIE, and $_SERVER.
 *
 * Also forces $_REQUEST to be $_GET + $_POST. If $_SERVER, $_COOKIE,
 * or $_ENV are needed, use those superglobals directly.
 *
 * @access private
 * @since 3.0.0
 */
function wp_magic_quotes() {
	// If already slashed, strip.
	if ( get_magic_quotes_gpc() ) {
		$_GET    = stripslashes_deep( $_GET    );
		$_POST   = stripslashes_deep( $_POST   );
		$_COOKIE = stripslashes_deep( $_COOKIE );
	}
	// Escape with wpdb.
	$_GET    = add_magic_quotes( $_GET    );
	$_POST   = add_magic_quotes( $_POST   );
	$_COOKIE = add_magic_quotes( $_COOKIE );
	$_SERVER = add_magic_quotes( $_SERVER );
	// Force REQUEST to be GET + POST.
	$_REQUEST = array_merge( $_GET, $_POST );
}
?><?php ?>
<?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?><?php ?>
