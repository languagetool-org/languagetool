		<!-- /MAIN TEXT -->

	</td>
</tr>
</table>

<p class="lastmod">Last modified:
	<?php 
	list($date, $time, $cet) = split(" ", $lastmod);
	print $date;
	?>
</p>
<p class="invisible">Time to generate page:
<?php
	print sprintf("%.2fs", getmicrotime()-$start_time);
?>
</p>

</body>
</html>
