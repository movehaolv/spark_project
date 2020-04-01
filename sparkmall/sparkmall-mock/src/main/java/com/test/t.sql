) AS t8 ON t8.module_id = t7.section_id
AND t8.online = t7.online

left join da.sd_stage_info t9 on t9.module_type=t1.module_type
	AND t9.cid = 4

WHERE
	t1.tag_code IS NOT NULL
	AND t1.{0} = ''{1}''