package com.example.controller;

import com.example.model.entity.VkGroup;
import com.example.service.PersistenceService;
import com.example.service.URI_Builder;
import com.example.service.exception.VkDataException;
import com.example.service.exception.VkHttpResponseException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;

/**
 * Description: контроллер
 * Creation date: 11.07.2016 8:20
 *
 * @author sks
 */

@Controller
public class GeneralController {

    Logger LOG = Logger.getLogger(this.getClass());
    
    private URI_Builder uriBuilder;
    private PersistenceService persistenceService;

    @RequestMapping(value = { "/", "/index" })
    public ModelAndView getIndex(@RequestParam(value = "response", required = false) String response,
            HttpSession session) {
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("response", response);
        return mav;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView getVkLoginRedirect(HttpSession session) {
        return new ModelAndView("redirect:" + uriBuilder.getVkAuthenticationURI());
    }

    @RequestMapping(value = { "/token" })
    public ModelAndView getAccessTokenPage(@RequestParam("code") String code, HttpSession session) {
        ModelAndView mav = new ModelAndView();
        try {
            persistenceService.addAccessToken(code, session.getId());
            mav.setViewName("redirect:/group_input");
        } catch (Exception exception) {
            exception.printStackTrace();
            LOG.error(exception.getMessage());
            mav.setViewName("redirect:/index?response=" + exception.getMessage());
        }
        return mav;
    }

    @RequestMapping(value = { "/group_input" }, method = RequestMethod.GET)
    public ModelAndView getGroupInputPage(@RequestParam(value = "response", required = false) String response,
            @RequestParam(value = "validation_errors", required = false) Boolean validationErrors,
            HttpSession session) {
        ModelAndView mav = new ModelAndView("group_input");
        mav.addObject("response", response);
        mav.addObject("validation_errors", validationErrors);
        return mav;
    }

    @RequestMapping(value = { "/group_input" }, method = RequestMethod.POST)
    public ModelAndView postGroup(@Valid VkGroup group, BindingResult bindingResult,
            HttpSession session) {
        ModelAndView mav = new ModelAndView();
        if (bindingResult.hasErrors()) {
            mav.setViewName("redirect:/group_input?response=Error&validation_errors=true");
        } else {
            try {
                persistenceService.addVkGroup(group);
            } catch (Exception exception) {
                exception.printStackTrace();
                LOG.error(exception.getMessage());
                mav.setViewName("redirect:/group_input?response=" + exception.getMessage());
                return mav;
            }
            mav.setViewName("redirect:/group_input?response=Ok");
        }
        return mav;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ModelAndView test() throws IOException, VkHttpResponseException, VkDataException {
        persistenceService.addNewPosts(124948366);
        return new ModelAndView("test");
    }

    @RequestMapping(value = "/group_list", method = RequestMethod.GET)
    public ModelAndView getGroupList(HttpSession session){
        ModelAndView mav = new ModelAndView();
        String viewName = "/group_list";
        try {
            mav.addAllObjects(persistenceService.getGroupList(session.getId()));
        } catch (Exception exception) {
            viewName = String.format("redirect:/index?response=%s", exception.getMessage());
            exception.printStackTrace();
            LOG.error(exception.getMessage());
        }
        mav.setViewName(viewName);
        return mav;
    }
    @Autowired
    public void setURI_Builder(URI_Builder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

    @Autowired
    public void setPersistenceService(PersistenceService persistenceService){
        this.persistenceService = persistenceService;
    }
}
