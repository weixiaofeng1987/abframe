package org.abframe.common;

import org.abframe.entity.Menu;
import org.abframe.util.Constant;
import org.abframe.util.MenuHelper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import java.util.List;
import java.util.Map;

public class PermissionHandler {

    /**
     * 访问权限及初始化按钮权限(控制按钮的显示)
     *
     * @param menuUrl 菜单路径
     * @return
     */
    public static boolean hasPerm(String menuUrl) {
        //判断是否拥有当前点击菜单的权限（内部过滤,防止通过url进入跳过菜单权限）
        /**
         * 根据点击的菜单的URL去菜单中的URL去匹配，当匹配到了此菜单，判断是否有此菜单的权限，没有的话跳转到404页面
         * 根据按钮权限，授权按钮(当前点的菜单和角色中各按钮的权限匹对)
         */
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        List<Menu> menuList = (List) session.getAttribute(Constant.SESSION_ALL_MENU_LIST);

        for (int i = 0; i < menuList.size(); i++) {
            List<Menu> subMenu = menuList.get(i).getSubMenu();

            for (int j = 0; j < subMenu.size(); j++) {
                if (subMenu.get(j).getMenuUrl().equals(menuUrl)) {
                    if (!subMenu.get(j).isHasMenu()) {
                        return false;
                    } else {                                                                //按钮判断
                        Map<String, String> map = (Map<String, String>) session.getAttribute(Constant.SESSION_QX);//按钮权限
                        map.remove("add");
                        map.remove("del");
                        map.remove("updateRoleById");
                        map.remove("cha");
                        int menuId = menuList.get(i).getSubMenu().get(j).getId();
                        String userName = session.getAttribute(Constant.SESSION_USERNAME).toString();    //获取当前登录者loginname
                        Boolean isAdmin = "admin".equals(userName);
                        map.put("add", (MenuHelper.testRights(map.get("adds"), menuId)) || isAdmin ? "1" : "0");
                        map.put("del", MenuHelper.testRights(map.get("dels"), menuId) || isAdmin ? "1" : "0");
                        map.put("updateRoleById", MenuHelper.testRights(map.get("edits"), menuId) || isAdmin ? "1" : "0");
                        map.put("cha", MenuHelper.testRights(map.get("chas"), menuId) || isAdmin ? "1" : "0");
                        session.removeAttribute(Constant.SESSION_QX);
                        session.setAttribute(Constant.SESSION_QX, map);    //重新分配按钮权限
                    }
                }
            }
        }
        return true;
    }

    /**
     * 按钮权限(方法中校验)
     *
     * @param menuUrl 菜单路径
     * @param type    类型(add、del、updateRoleById、cha)
     * @return
     */
    public static boolean buttonJurisdiction(String menuUrl, String type) {
        //判断是否拥有当前点击菜单的权限（内部过滤,防止通过url进入跳过菜单权限）
        /**
         * 根据点击的菜单的xxx.do去菜单中的URL去匹配，当匹配到了此菜单，判断是否有此菜单的权限，没有的话跳转到404页面
         * 根据按钮权限，授权按钮(当前点的菜单和角色中各按钮的权限匹对)
         */
        //shiro管理的session
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        //获取菜单列表
        List<Menu> menuList = (List) session.getAttribute(Constant.SESSION_ALL_MENU_LIST);

        for (int i = 0; i < menuList.size(); i++) {
            for (int j = 0; j < menuList.get(i).getSubMenu().size(); j++) {
                if (menuList.get(i).getSubMenu().get(j).getMenuUrl().equals(menuUrl)) {
                    if (!menuList.get(i).getSubMenu().get(j).isHasMenu()) {                //判断有无此菜单权限
                        return false;
                    } else {                                                                //按钮判断
                        Map<String, String> map = (Map<String, String>) session.getAttribute(Constant.SESSION_QX);//按钮权限
                        int menuId = menuList.get(i).getSubMenu().get(j).getId();
                        String userName = session.getAttribute(Constant.SESSION_USERNAME).toString();
                        Boolean isAdmin = "admin".equals(userName);
                        if ("add".equals(type)) {
                            return ((MenuHelper.testRights(map.get("adds"), menuId)) || isAdmin);
                        } else if ("del".equals(type)) {
                            return ((MenuHelper.testRights(map.get("dels"), menuId)) || isAdmin);
                        } else if ("updateRoleById".equals(type)) {
                            return ((MenuHelper.testRights(map.get("edits"), menuId)) || isAdmin);
                        } else if ("cha".equals(type)) {
                            return ((MenuHelper.testRights(map.get("chas"), menuId)) || isAdmin);
                        }
                    }
                }
            }
        }
        return true;
    }

}
