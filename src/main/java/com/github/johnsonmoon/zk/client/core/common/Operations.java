package com.github.johnsonmoon.zk.client.core.common;

import java.util.List;

/**
 * 
 * Created by xuyh at 2017年12月14日 下午2:04:45.
 */
public class Operations {
	/**
	 * 判断finalNodePath在paths列表中路径编号是否最小
	 * 
	 * @param baseNodePath
	 *            父节点路径
	 * @param childNodePath
	 *            子节点基础路径
	 * @param finalNodePath
	 *            当前节点最终路径
	 * @param paths
	 *            子节点路径列表
	 * @return true/false
	 */
	public static boolean judgePathNumMin(String baseNodePath, String childNodePath, String finalNodePath,
			List<String> paths) {
		if (paths.isEmpty())
			return true;
		if (paths.size() >= 2) {
			// 对无序状态的path列表按照编号升序排序
			paths.sort((str1, str2) -> {
				int num1;
				int num2;
				String string1 = str1.substring(childNodePath.length(), str1.length());
				String string2 = str2.substring(childNodePath.length(), str2.length());
				num1 = Integer.parseInt(string1);
				num2 = Integer.parseInt(string2);
				if (num1 > num2) {
					return 1;
				} else if (num1 < num2) {
					return -1;
				} else {
					return 0;
				}
			});
		}

		String minId = paths.get(0);
		return finalNodePath.equals(baseNodePath + "/" + minId);
	}
}
