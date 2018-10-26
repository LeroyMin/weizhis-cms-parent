package cn.weizhis.cms.controller.system;

import cn.weizhis.cms.common.Constant;
import cn.weizhis.cms.common.InvokeResult;
import cn.weizhis.cms.common.Page;
import cn.weizhis.cms.common.Query;
import cn.weizhis.cms.entity.system.SysUserEntity;
import cn.weizhis.cms.service.system.SysUserRoleService;
import cn.weizhis.cms.service.system.SysUserService;
import cn.weizhis.cms.shiro.ShiroUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: minliang
 * @Date: 2018/10/8 11:04
 * @Description:
 */
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends AbstractController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserRoleService sysUserRoleService;

    /**
     * 所有用户列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("sys:user:list")
    public InvokeResult list(@RequestParam Map<String, Object> params){
        InvokeResult result = new InvokeResult();
        //只有超级管理员，才能查看所有管理员列表
        if(getUserId() != Constant.SUPER_ADMIN){
            params.put("createUserId", getUserId());
        }

        //查询列表数据
        Query query = new Query(params);
        List<SysUserEntity> userList = sysUserService.queryList(query);
        int total = sysUserService.queryTotal(query);
        Page pageUtil = new Page(userList, total, query.getLimit(), query.getPage());
        result.setData(pageUtil);
        return result;
    }

    /**
     * 获取登录的用户信息
     */
    @RequestMapping("/info")
    public InvokeResult info(){
        InvokeResult result = new InvokeResult();
//        if (StringUtils.isBlank(token))
//            return result.failure("登录失效");
//        Serializable sessionId = ShiroUtils.getSubject().getSession().getId();
//        if (!token.equals(sessionId)){
//            return result.failure("登录失效");
//        }
        result.setData(getUser());
        return result;
    }

    /**
     * 修改登录用户密码
     */
    @RequestMapping("/password")
    public InvokeResult password(String password, String newPassword){
//        Assert.isBlank(newPassword, "新密码不为能空");
        InvokeResult result = new InvokeResult();
        //sha256加密
        password = new Sha256Hash(password).toHex();
        //sha256加密
        newPassword = new Sha256Hash(newPassword).toHex();

        //更新密码
        int count = sysUserService.updatePassword(getUserId(), password, newPassword);
        if(count == 0){
            return result.failure("原密码不正确");
        }
        //退出
        ShiroUtils.logout();

        return result.sucess();
    }

    /**
     * 用户信息
     */
    @RequestMapping("/info/{userId}")
    @RequiresPermissions("sys:user:info")
    public InvokeResult info(@PathVariable("userId") Long userId){
        InvokeResult result = new InvokeResult();
        SysUserEntity user = sysUserService.queryUserByUserId(userId);

        //获取用户所属的角色列表
        List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);
        user.setRoleIdList(roleIdList);
        result.setData(new HashMap<String,Object>().put("user", user));
        return result;
    }

    /**
     * 保存用户
     */
    @RequestMapping("/save")
    @RequiresPermissions("sys:user:save")
    public InvokeResult save(@RequestBody SysUserEntity user){
        InvokeResult result = new InvokeResult();
//        ValidatorUtils.validateEntity(user, AddGroup.class);

        user.setCreateUserId(getUserId());
        sysUserService.save(user);

        return result.sucess();
    }

    /**
     * 修改用户
     */
    @RequestMapping("/update")
    @RequiresPermissions("sys:user:update")
    public InvokeResult update(@RequestBody SysUserEntity user){
        InvokeResult result = new InvokeResult();
//        ValidatorUtils.validateEntity(user, UpdateGroup.class);

        user.setCreateUserId(getUserId());
        sysUserService.updateUser(user);

        return result.sucess();
    }

    /**
     * 删除用户
     */
    @RequestMapping("/delete")
    @RequiresPermissions("sys:user:delete")
    public InvokeResult delete(@RequestBody Long[] userIds){
        InvokeResult result = new InvokeResult();
        if(ArrayUtils.contains(userIds, 1L)){
            return result.failure("系统管理员不能删除");
        }

        if(ArrayUtils.contains(userIds, getUserId())){
            return result.failure("当前用户不能删除");
        }

        sysUserService.deleteBatch(userIds);

        return result.sucess();
    }

}