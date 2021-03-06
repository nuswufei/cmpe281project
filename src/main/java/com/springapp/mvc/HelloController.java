package com.springapp.mvc;

import com.springapp.mvc.Authentication.AccountInfo;
import com.springapp.mvc.Authentication.AccountInfoDAO;
import com.springapp.mvc.Data.DataDAO;
import com.springapp.mvc.Data.DataPoint;
import com.springapp.mvc.Data.Usage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/*")
public class HelloController {
    AccountInfoDAO accountInfoDAO = new AccountInfoDAO();
    DataDAO dataDAO = new DataDAO();
    @RequestMapping(value = "register", method = RequestMethod.POST)
    @ResponseBody
    AccountInfo register(@RequestParam String name, @RequestParam String password) {
        AccountInfo accountInfo = new AccountInfo(name, password);
        if(accountInfoDAO.insert(accountInfo)) {
            return accountInfo;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/admin/deregister/{name}", method = RequestMethod.DELETE)
    void adminDeregister(@PathVariable String name) {
        accountInfoDAO.delete(name);
        dataDAO.deleteByName(name);
    }

    @RequestMapping(value = "/user/deregister", method = RequestMethod.DELETE)
    void deregister(final HttpServletRequest request) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        accountInfoDAO.delete(name);
        dataDAO.deleteByName(name);
    }

    @RequestMapping(value = "/user/getdata", method = RequestMethod.GET)
    @ResponseBody
    List<DataPoint> userGetData(final HttpServletRequest request) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<DataPoint> result = dataDAO.findByName(name);
        return result;
    }

    @RequestMapping(value = "/admin/getdata", method = RequestMethod.GET)
    @ResponseBody
    Map<String, List<DataPoint>> adminGetData() {
        Map<String, List<DataPoint>> result = new HashMap<String, List<DataPoint>>();
        List<String> allNames = accountInfoDAO.getAllName();
        for(String name : allNames) {
            result.put(name, dataDAO.findByName(name));
        }
        return result;
    }

    @RequestMapping(value = "/user/getabnormaldata", method = RequestMethod.GET)
    @ResponseBody
    List<DataPoint> userGetAbnormalData(final HttpServletRequest request) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        List<DataPoint> result = dataDAO.findAbnormalByName(name);
        return result;
    }

    @RequestMapping(value = "/admin/getabnormaldata", method = RequestMethod.GET)
    @ResponseBody
    Map<String, List<DataPoint>> adminGetAbnormalData() {
        Map<String, List<DataPoint>> result = new HashMap<String, List<DataPoint>>();
        List<String> allNames = accountInfoDAO.getAllName();
        for(String name : allNames) {
            result.put(name, dataDAO.findAbnormalByName(name));
        }
        return result;
    }


    @RequestMapping(value = "/user/getusage", method = RequestMethod.GET)
    @ResponseBody
    Usage userGetUsage(final HttpServletRequest request) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        int credit = accountInfoDAO.getAccountInfo(name).getCredit();
        int dataCount = dataDAO.getDatasize(name);
        Usage usage = new Usage(dataCount, credit);
        return usage;
    }

    @RequestMapping(value = "/user/topup", method = RequestMethod.POST)
    @ResponseBody
    Usage userTopup(final HttpServletRequest request, @RequestParam int credit) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        AccountInfo accountInfo = accountInfoDAO.getAccountInfo(name);
        accountInfoDAO.delete(accountInfo.getName());
        accountInfo.setCredit(accountInfo.getCredit() + credit);
        accountInfoDAO.insert(accountInfo);
        return userGetUsage(request);
    }

    @RequestMapping(value = "/admin/getusage", method = RequestMethod.GET)
    @ResponseBody
    Map<String, Usage> adminGetUsage() {
        Map<String, Usage> result = new HashMap<String, Usage>();
        List<String> allnames = accountInfoDAO.getAllName();
        for(String name : allnames) {
            int credit = accountInfoDAO.getAccountInfo(name).getCredit();
            int dataCount = dataDAO.getDatasize(name);
            Usage usage = new Usage(dataCount, credit);
            result.put(name, usage);
        }
        return result;
    }

    @RequestMapping(value = "/uploaddata", method = RequestMethod.POST)
    @ResponseBody DataPoint upload(@RequestBody DataPoint dataPoint) {
        if(dataDAO.insert(dataPoint)) {
            return dataPoint;
        }
        return null;
    }

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {
		model.addAttribute("message", "Hello world!");
		return "hello";
	}

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public String testLogin(ModelMap model) {

        AccountInfo accountInfo = accountInfoDAO.getAccountInfo("user1");
        if(accountInfo != null)
        model.addAttribute("message", accountInfo.getName() + accountInfo.getPassword());
        else
            model.addAttribute("message", "cannot find");
        return "hello";
    }

    @RequestMapping(value = "user/getname", method = RequestMethod.GET)
    public String testUserAuth(ModelMap model, final HttpServletRequest request) {
        model.addAttribute("message", SecurityContextHolder.getContext().getAuthentication().getName());
        return "hello";
    }
    @RequestMapping(value = "admin", method = RequestMethod.GET)
    public String testAdminAuth() {
        return "writeconfirm";
    }
}