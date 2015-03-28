/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.po.share;

import org.alfresco.po.share.site.document.UserProfile;
import org.alfresco.webdrone.*;
import org.alfresco.webdrone.exception.PageException;
import org.alfresco.webdrone.exception.PageOperationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.alfresco.webdrone.RenderElement.getVisibleRenderElement;

/**
 * @author Charu To get the list of groups and group members this page is used
 */
public class GroupsPage extends SharePage
{
    private static Log logger = LogFactory.getLog(GroupsPage.class);

    private static final String SHOW_ALL_LABEL = "label[for$='_default-show-all']";
    private static final String SHOW_ALL_CHK_BOX = "input[id$='_default-show-all']";

    private static final String BUTTON_BROWSE = "button[id$='default-browse-button-button']";
    private static final String BUTTON_SEARCH = "button[id$='default-search-button-button']";
    private static final String BUTTON_ADD = ".groups-newgroup-button";
    private static final String GROUP_NAMES = "a[class$='groups-item-group']";

    private static final String USER_NAMES = "a[class*='groups-item-user']>span[class$='-item-label']";
    private static final By NEW_SUBGROUP_BUTTON = By.cssSelector("span[title='New Subgroup']");
    private static final By ADD_GROUP_BUTTON = By.cssSelector("span[title='Add Group']");
    private static final By ADD_USER_BUTTON = By.cssSelector("span[title='Add User']");
    private static final String USER_ADDED = "//a[contains(@class, 'groups-item-user')]/span[contains(text(), '%s')]";
    private static final By DELETE_GROUP_FORM = By.cssSelector("div[id*='deletegroupdialog_h']");
    private static final By DELETE_GROUP_BUTTON = By.cssSelector("button[id*='default-remove-button-button']");
    private static final By CANCEL_GROUP_BUTTON = By.cssSelector("button[id*='default-cancel-button-button']");
    private static final String ADD_USER_ICON = ".groups-adduser-button";
    @SuppressWarnings("unused")
    private static final String EDIT_GROUP_ICON = ".groups-update-button";

    public GroupsPage(WebDrone drone)
    {
        super(drone);
    }

    @SuppressWarnings("unchecked")
    @Override
    public GroupsPage render(RenderTime timer)
    {
        RenderElement actionMessage = getActionMessageElement(ElementState.INVISIBLE);
        elementRender(timer, getVisibleRenderElement(By.cssSelector(BUTTON_BROWSE)), getVisibleRenderElement(By.cssSelector(BUTTON_SEARCH)),
                getVisibleRenderElement(By.cssSelector(SHOW_ALL_LABEL)), getVisibleRenderElement(By.cssSelector(SHOW_ALL_CHK_BOX)), actionMessage);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GroupsPage render(final long time)
    {
        return render(new RenderTime(time));
    }

    @SuppressWarnings("unchecked")
    @Override
    public GroupsPage render()
    {
        return render(new RenderTime(maxPageLoadingTime));
    }

    /**
     * @return
     */
    public GroupsPage clickBrowse()
    {
        drone.findAndWait(By.cssSelector(BUTTON_BROWSE)).click();

        return this;
    }

    /**
     * @return
     */
    public NewGroupPage navigateToNewGroupPage()
    {
        try
        {
            drone.findAndWait(By.cssSelector(BUTTON_ADD)).click();
            return new NewGroupPage(drone);
        }
        catch (StaleElementReferenceException e)
        {
            return navigateToNewGroupPage();
        }
    }

    /**
     * Get list of available groups.
     * 
     * @return
     */
    public List<String> getGroupList()
    {
        List<String> nameOfGroups = new ArrayList<String>();
        List<WebElement> groupElements = drone.findAndWaitForElements(By.cssSelector(GROUP_NAMES));
        for (WebElement webElement : groupElements)
        {
            nameOfGroups.add(webElement.getText());
        }
        return nameOfGroups;
    }

    /**
     * Select Group name from available group list in the Groups page
     * 
     * @param groupName -To select this group name from the list of groups
     * @return {@link GroupsPage}
     */
    public GroupsPage selectGroup(String groupName)
    {
        WebDroneUtil.checkMandotaryParam("Group Name", groupName);
        try
        {
            for (WebElement name : drone.findAndWaitForElements(By.cssSelector(GROUP_NAMES)))
            {
                if (groupName.equalsIgnoreCase(name.getText()))
                {
                    name.click();
                    return this;
                }
            }
        }
        catch (TimeoutException e)
        {
            logger.error("Unable to get the group from list of Groups : " + e.getMessage());
        }

        throw new PageOperationException("Unable to select group : " + groupName);
    }

    /**
     * Assert method to verify any group name is present in the list of groups in groups page
     * 
     * @param GroupName -To verify this group name is present in the the list of groups
     * @return Boolean
     */
    public boolean isGroupPresent(String GroupName)
    {
        WebDroneUtil.checkMandotaryParam("Group Name", GroupName);
        try
        {
            List<WebElement> groupList = drone.findAll(By.cssSelector(GROUP_NAMES));

            for (WebElement groupName : groupList)
            {
                if (groupName.getText().contains(GroupName))
                {
                    return true;
                }
            }
        }
        catch (NoSuchElementException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Unable to find group in list");
            }
        }
        return false;
    }

    /**
     * Assert method to verify any group name is present in the list of groups in groups page
     * 
     * @param groupName -To verify this group name is present in the the list of groups
     * @return int
     */
    public int countGroupPresent(String groupName)
    {
        WebDroneUtil.checkMandotaryParam("Group Name", groupName);
        int count = 0;
        try
        {
            List<WebElement> groupList = drone.findAll(By.cssSelector(GROUP_NAMES));

            for (WebElement group : groupList)
            {
                if (group.getText().contains(groupName))
                {
                    count++;
                }
            }
        }
        catch (NoSuchElementException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Unable to find group in list");
            }
        }
        return count;
    }

    /**
     * Get list of group members for any group in groups page
     * 
     * @return List of Users
     */
    public List<UserProfile> getMembersList()
    {
        try
        {
            List<UserProfile> listOfUsers = new ArrayList<UserProfile>();
            List<WebElement> groupMembers = drone.findAndWaitForElements(By.cssSelector(USER_NAMES));
            for (WebElement webElement : groupMembers)
            {
                UserProfile profile = new UserProfile();
                String text = webElement.getText();
                StringTokenizer userInfo = new StringTokenizer(text);

                int userDispalySize = 0;
                int userInfoSize = userInfo.countTokens();

                while (userInfo.hasMoreElements())
                {

                    if (userDispalySize == 0)
                    {
                        profile.setfName((String) userInfo.nextElement());
                    }
                    else if (userDispalySize == 1 && userInfoSize > 2)
                    {
                        profile.setlName((String) userInfo.nextElement());
                    }
                    else if (userDispalySize == 2 || (userDispalySize == 1 && userInfoSize == 2))
                    {
                        profile.setUsername((String) userInfo.nextElement());
                    }
                    userDispalySize++;
                }
                listOfUsers.add(profile);
            }
            return listOfUsers;

        }
        catch (TimeoutException e)
        {
            logger.error("Unable to get the list of members : ", e);
        }

        throw new PageOperationException("Unable to get the list of members  : ");
    }

    /**
     * click on Add user button in GroupsPage
     * 
     * @return AddUserPage
     */
    /*
     * public AddUserPage clickAddUser()
     * {
     * try
     * {
     * WebElement addUserButton = drone.findAndWait(By.cssSelector(BUTTON_ADD_USER));
     * if (addUserButton.isDisplayed() && addUserButton.isEnabled())
     * {
     * addUserButton.click();
     * return new AddUserPage(drone);
     * }
     * } catch (TimeoutException nse)
     * {
     * logger.error("Unable to find Add User button");
     * }
     * throw new PageOperationException("Not found element is : " + BUTTON_ADD_USER);
     * }
     *//**
     * Click on Add Group button in GroupsPage
     * 
     * @return AddGroupPage
     */
    /*
     * public AddGroupPage clickAddGroup()
     * {
     * try
     * {
     * WebElement addGroupButton = drone.findAndWait(By.cssSelector(BUTTON_ADD_GROUP));
     * if (addGroupButton.isDisplayed())
     * {
     * addGroupButton.click();
     * return new AddGroupPage(drone);
     * }
     * } catch (TimeoutException nse)
     * {
     * logger.error("Unable to find Add Group button");
     * }
     * throw new PageOperationException("Not found element is : " + BUTTON_ADD_GROUP);
     * }
     */

    /**
     * Click on Remove icon on members list in group page
     * 
     * @param userName -To remove the userName from the list of Users in the groups page
     * @return RemoveUserFromGroupPage
     */
    public RemoveUserFromGroupPage removeUser(String userName)
    {
        if (StringUtils.isEmpty(userName))
        {
            throw new IllegalArgumentException("User Name is required.");
        }

        try
        {
            WebElement element = drone.findAndWait(By.xpath(String.format(".//span[contains(text(),'%s')]/..", userName)));
            drone.mouseOver(element);
            element = element.findElement(By.cssSelector("span.yui-columnbrowser-item-buttons>span.users-remove-button"));
            drone.mouseOver(element);
            element.click();
            return new RemoveUserFromGroupPage(getDrone());
        }
        catch (TimeoutException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("User: \"" + userName + "\" can not be found in members list.", e);
            }
        }
        throw new PageException("User: \"" + userName + "\" can not be found in members list.");
    }

    /**
     * Verify list of users are displayed in Group page
     * 
     * @return Boolean
     */
    public boolean hasMembers()

    {

        try
        {
            WebElement element = drone.find(By.cssSelector(USER_NAMES));
            if (element.isDisplayed())
            {
                return true;
            }

        }
        catch (NoSuchElementException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Unable to find member in  list", e);
            }
        }
        return false;
    }

    /**
     * Verify User is the member of group
     * 
     * @return Boolean
     */
    // , String groupName
    public boolean isUserGroupMember(String fName, String lName)
    {
        List<UserProfile> userProfiles = this.getMembersList();

        for (UserProfile userProfile : userProfiles)
        {
            if (fName.equals(userProfile.getfName()))
            {
                // Verify user is present in the members list
                if (userProfile.getUsername().contains(lName))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verify User is Admin
     * 
     * @return Boolean
     */

    public Boolean isUserAdmin(String fName, String lName)
    {
        String siteAdmin = "ALFRESCO_ADMINISTRATORS";

        selectGroup(siteAdmin);
        List<UserProfile> userProfiles = this.getMembersList();

        for (UserProfile userProfile : userProfiles)
        {
            if (fName.equals(userProfile.getfName()))
            {
                // Verify user is present in the members list
                if (userProfile.getUsername().contains(lName))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Click on the group (select it) and then click to the New Subgroup
     * 
     * @return NewGroupPage
     */
    public NewGroupPage navigateToCreateNewSubGroup(String parentGroup)
    {
        selectGroup(parentGroup).render();
        drone.findAndWait(NEW_SUBGROUP_BUTTON).click();
        return new NewGroupPage(drone);
    }

    /**
     * Click on the group (select it) and then click to the Add Group
     * 
     * @return AddGroupForm
     */
    public AddGroupForm navigateToAddGroupForm(String group)
    {
        selectGroup(group).render();
        drone.findAndWait(ADD_GROUP_BUTTON).click();
        return new AddGroupForm(drone).render();
    }

    /**
     * Click on the group (select it) and then click to the Add User
     * 
     * @return AddUserToGroupForm
     */
    public AddUserToGroupForm navigateToAddUserForm(String group)
    {
        selectGroup(group).render();
        drone.findAndWait(ADD_USER_BUTTON).click();
        return new AddUserToGroupForm(drone).render();
    }

    /**
     * Verify that user was added to the group and displayed
     * 
     * @return Boolean
     */
    public boolean isUserDisplayed(String user)
    {
        try
        {
            drone.getCurrentPage().render();
            WebElement userElem = drone.find(By.xpath(String.format(USER_ADDED, user)));
            if (userElem.isDisplayed())
            {
                return true;
            }
        }
        catch (NoSuchElementException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Unable to find user in list", e);
            }
        }
        return false;

    }

    /**
     * Click on Edit icon on group in group page
     * 
     * @param groupName -To edit the group from the list of groups in the groups page
     * @return EditGroupPage
     */
    private EditGroupPage selectEditGroupAction(String groupName)
    {
        if (StringUtils.isEmpty(groupName))
        {
            throw new IllegalArgumentException("Group Name is required.");
        }

        try
        {
            WebElement element = drone.findAndWait(By.xpath(String.format("//span[contains(text(),'%s')]/..", groupName)));
            drone.mouseOver(element);
            element = element.findElement(By.xpath(String.format("//span[contains(text(),'%s')]/..", groupName) + "//span[@class='groups-update-button']"));
            drone.mouseOver(element);
            element.click();
            return new EditGroupPage(getDrone());
        }
        catch (TimeoutException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Group: \"" + groupName + "\" can not be found in groups list.", e);
            }
        }
        throw new PageException("Group: \"" + groupName + "\" can not be found in groups list.");
    }

    public GroupsPage editGroup(String groupName, String newGroupName, boolean edit)
    {
        EditGroupPage editGroupPage = selectEditGroupAction(groupName).render();
        return editGroupPage.editGroup(newGroupName, edit).render();

    }

    /**
     * Click on Delete icon on group in group page
     * 
     * @param groupName -To delete the group from the list of groups in the groups page
     * @return GroupsPage
     */
    public GroupsPage deleteGroup(String groupName, boolean delete)
    {
        if (StringUtils.isEmpty(groupName))
        {
            throw new IllegalArgumentException("Group Name is required.");
        }

        try
        {
            WebElement element = drone.findAndWait(By.xpath(String.format("//span[contains(text(),'%s')]/..", groupName)));
            drone.mouseOver(element);
            element = element.findElement(By.xpath(String.format("//span[contains(text(),'%s')]/..", groupName) + "//span[@class='groups-delete-button']"));
            drone.mouseOver(element);
            element.click();
            drone.findAndWait(DELETE_GROUP_FORM);
            if (delete)
            {
                WebElement deleteButton = drone.findAndWait(DELETE_GROUP_BUTTON);
                deleteButton.click();
            }
            else
            {
                WebElement cancelButton = drone.findAndWait(CANCEL_GROUP_BUTTON);
                cancelButton.click();

            }

            return new GroupsPage(getDrone());
        }
        catch (TimeoutException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Group: \"" + groupName + "\" can not be found and deleted from groups list.", e);
            }
        }
        throw new PageException("Group: \"" + groupName + "\" can not be found and deleted from groups list.");
    }

    /**
     * Click add user icon
     * 
     * @return AddUserGroupPage
     */
    public AddUserGroupPage selectAddUser()
    {
        try
        {
            WebElement addUser = drone.findAndWait(By.cssSelector(ADD_USER_ICON));
            addUser.click();

            return new AddUserGroupPage(drone);
        }
        catch (TimeoutException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace(ADD_USER_ICON + "isn't present at a page", e);
            }
        }
        throw new PageException("Add user dialogue isn't opened");
    }

    /**
     * Get list of added users
     * 
     * @return
     */
    public List<String> getUserList()
    {
        List<String> nameOfUsers = new ArrayList<String>();
        List<WebElement> userElements = drone.findAndWaitForElements(By.cssSelector(USER_NAMES));
        for (WebElement webElement : userElements)
        {
            nameOfUsers.add(webElement.getText());
        }
        return nameOfUsers;
    }

    /**
     * Click on Edit icon on list of groups in group page
     * 
     * @param groupName
     * @return EditGroupPage
     */
    public EditGroupPage selectEditGroup(String groupName)
    {
        try
        {
            WebElement first_column_group_locator = drone.findAndWait(By.xpath("//span[contains(@class,'yui-columnbrowser-item-label') and contains(text(),'"
                    + groupName + "')]"));
            first_column_group_locator.click();
            drone.mouseOver(first_column_group_locator);
            WebElement edit_group_button = drone.findAndWait(By.xpath("//span[contains(text(),'" + groupName
                    + "')]/parent::*/span/span[contains(@class,'groups-update-button')]"));
            edit_group_button.click();
            return new EditGroupPage(drone);
        }
        catch (TimeoutException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace(groupName + " isn't displayed", e);
            }
        }
        throw new PageException(" Edit group page isn't opened");
    }

    /**
     * Click on Delete icon on list of groups in group page
     * 
     * @param groupName
     */
    public DeleteGroupFromGroupPage deleteGroup(String groupName)
    {
        try
        {
            WebElement first_column_group_locator = drone.findAndWait(By.xpath("//span[contains(@class,'yui-columnbrowser-item-label') and contains(text(),'"
                    + groupName + "')]"));
            first_column_group_locator.click();
            drone.mouseOver(first_column_group_locator);
            WebElement delete_group_button = drone.findAndWait(By.xpath("//*[contains(text(),'" + groupName
                    + "')]/ancestor::a//span[contains(@class, 'delete')]"));
            delete_group_button.click();
            return new DeleteGroupFromGroupPage(drone);

        }
        catch (TimeoutException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace(groupName + " isn't displayed", e);
            }
        }
        throw new PageException(" Delete group page isn't opened");
    }

    /**
     * Click on Remove icon on members list in group page
     * 
     * @param userName -To remove the userName from the list of Users in the groups page
     * @return RemoveUserFromGroupPage
     */
    public RemoveUserFromGroupPage selectRemoveUser(String userName)
    {
        if (StringUtils.isEmpty(userName))
        {
            throw new IllegalArgumentException("User Name is required.");
        }

        try
        {
            WebElement element = drone.findAndWait(By.xpath("//li[2]//span[contains(text(),'" + userName + "')]"));
            drone.mouseOver(element);
            element = element.findElement(By.xpath("//span[@class='users-remove-button']"));
            element.click();
            return new RemoveUserFromGroupPage(getDrone());
        }
        catch (TimeoutException e)
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("User: \"" + userName + "\" can not be found in members list.", e);
            }
        }
        throw new PageException("User: \"" + userName + "\" can not be found in members list.");
    }

}
