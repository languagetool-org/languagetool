/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.gui;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.UserConfig;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.rules.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Support for associating a LanguageTool instance and a JTextComponent
 *
 * @author Panagiotis Minos
 * @since 2.3
 */
class LanguageToolSupport {

  static final String CONFIG_FILE = ".languagetool.cfg";

  //maximum entries in the activate rule menu.
  //If entries' number is bigger, create per category submenus
  //can set to 0 to always create category submenus
  private static final int MAX_RULES_NO_CATEGORY_MENU = 12;
  //maximum rule menu entries, if more create a More submenu
  private static final int MAX_RULES_PER_MENU = 12;
  //maximum category menu entries, if more create a More submenu
  private static final int MAX_CATEGORIES_PER_MENU = 12;

  private final UndoRedoSupport undo;
  private final LanguageIdentifier langIdentifier;
  private final JFrame frame;
  private final JTextComponent textComponent;
  private final EventListenerList listenerList = new EventListenerList();
  private final ResourceBundle messages;
  private final List<RuleMatch> ruleMatches;
  private final List<Span> documentSpans;

  private MultiThreadedJLanguageTool languageTool;
  private ScheduledExecutorService checkExecutor;
  private MouseListener mouseListener;
  private ActionListener actionListener;
  private int millisecondDelay = 1500;
  private AtomicInteger check;
  private boolean popupMenuEnabled = true;
  private boolean backgroundCheckEnabled = true;
  private Configuration config;
  private boolean mustDetectLanguage = false;

  /**
   * LanguageTool support for a JTextComponent
   */
  LanguageToolSupport(JFrame frame, JTextComponent textComponent) {
    this(frame, textComponent, null);
  }

  /**
   * LanguageTool support for a JTextComponent
   * @since 2.7
   */
  LanguageToolSupport(JFrame frame, JTextComponent textComponent, UndoRedoSupport support) {
    this.frame = frame;
    this.textComponent = textComponent;
    this.messages = JLanguageTool.getMessageBundle();
    ruleMatches = new ArrayList<>();
    documentSpans = new ArrayList<>();    
    this.undo = support;
    this.langIdentifier = new LanguageIdentifier();
    init();
  }

  void addLanguageToolListener(LanguageToolListener ltListener) {
    listenerList.add(LanguageToolListener.class, ltListener);
  }

  void removeLanguageToolListener(LanguageToolListener ltListener) {
    listenerList.remove(LanguageToolListener.class, ltListener);
  }

  private void fireEvent(LanguageToolEvent.Type type, Object caller, long elapsedTime) {
    LanguageToolEvent event = new LanguageToolEvent(this, type, caller, elapsedTime);
    fireEvent(event);
  }

  private void fireEvent(LanguageToolEvent.Type type, Object caller) {
    LanguageToolEvent event = new LanguageToolEvent(this, type, caller);
    fireEvent(event);
  }

  private void fireEvent(LanguageToolEvent event) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == LanguageToolListener.class) {
        // Lazily create the event:
        ((LanguageToolListener) listeners[i + 1]).languageToolEventOccurred(event);
      }
    }
  }

  JTextComponent getTextComponent() {
    return textComponent;
  }

  List<RuleMatch> getMatches() {
    return this.ruleMatches;
  }

  void reloadConfig() {
    //FIXME
    //if mother tongue changes then create new JLanguageTool instance

    boolean update = false;
  
    Language language = languageTool.getLanguage();
    languageTool = new MultiThreadedJLanguageTool(language, config.getMotherTongue(), 
        new UserConfig(config.getConfigurableValues()));
    config.initStyleCategories(languageTool.getAllRules());

    Set<String> disabledRules = config.getDisabledRuleIds();
    if (disabledRules == null) {
      disabledRules = Collections.emptySet();
    }

    Set<String> common = new HashSet<>(disabledRules);
    common.retainAll(languageTool.getDisabledRules());
    Set<String> toDisable = new HashSet<>(disabledRules);
    toDisable.removeAll(common);
    Set<String> toEnable = new HashSet<>(languageTool.getDisabledRules());
    toEnable.removeAll(common);
    
    for (String ruleId : toDisable) {
      languageTool.disableRule(ruleId);
      update = true;
    }
    for (String ruleId : toEnable) {
      languageTool.enableRule(ruleId);
      update = true;
    }

    Set<String> disabledCategoryNames = config.getDisabledCategoryNames();
    if (disabledCategoryNames == null) {
      disabledCategoryNames = Collections.emptySet();
    }
    Set<CategoryId> disabledCategories = new HashSet<>();
    Map<CategoryId, Category> langCategories = languageTool.getCategories();
    
    for (CategoryId id : langCategories.keySet()) {
      String categoryName = langCategories.get(id).getName();
      if (disabledCategoryNames.contains(categoryName)) {
        disabledCategories.add(id);
      }
    }

    Set<CategoryId> ltDisabledCategories = new HashSet<>();
    for (CategoryId id : langCategories.keySet()) {
      if (languageTool.isCategoryDisabled(id)) {
        ltDisabledCategories.add(id);
      }
    }
    
    Set<CategoryId> commonCat = new HashSet<>(disabledCategories);
    commonCat.retainAll(ltDisabledCategories);

    Set<CategoryId> toDisableCat = new HashSet<>(disabledCategories);
    toDisableCat.removeAll(commonCat);

    Set<CategoryId> toEnableCat = new HashSet<>(ltDisabledCategories);
    toEnableCat.removeAll(commonCat);

    for(CategoryId id : toDisableCat) {
      languageTool.disableCategory(id);
    }
    for(CategoryId id : toEnableCat) {
      languageTool.enableRuleCategory(id);
    }      
    if (!toDisableCat.isEmpty() || !toEnableCat.isEmpty()) {
      // ugly hack to trigger reInitSpellCheckIgnoreWords()
      update = true;
    }

    Set<String> enabledRules = config.getEnabledRuleIds();
    if (enabledRules == null) {
      enabledRules = Collections.emptySet();
    }
    for (String ruleName : enabledRules) {
      languageTool.enableRule(ruleName);
      update = true;
    }
    
//    languageTool.setConfigValues(config.getConfigValues());

    if (update) {
      //FIXME
      //we could skip a full check if the user disabled but didn't enable rules
      checkImmediately(null);
      fireEvent(LanguageToolEvent.Type.RULE_ENABLED, null);
    }
  }

  private void reloadLanguageTool(Language language) {
    try {
      //FIXME
      //no need to read again the file
      config = new Configuration(new File(System.getProperty("user.home")), CONFIG_FILE, language);
      //config still contains old language, update it
      this.config.setLanguage(language);
      // Calling shutdown here may cause a RejectedExecutionException:
      //if (languageTool != null) {
      //  languageTool.shutdownWhenDone();
      //}
      languageTool = new MultiThreadedJLanguageTool(language, config.getMotherTongue(), 
          new UserConfig(config.getConfigurableValues()));
      config.initStyleCategories(languageTool.getAllRules());
      languageTool.setCleanOverlappingMatches(false);
      Tools.configureFromRules(languageTool, config);
      activateLanguageModelRules(language);
      activateWord2VecModelRules(language);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void activateLanguageModelRules(Language language) {
    if (config.getNgramDirectory() != null) {
      File ngramLangDir = new File(config.getNgramDirectory(), language.getShortCode());
      if (ngramLangDir.exists()) {
        try {
          languageTool.activateLanguageModelRules(config.getNgramDirectory());
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "Error while loading ngram database.\n" + e.getMessage());
        }
      } else {
        // user might have set ngram directory to use it for e.g. English, but they
        // might not have the data for other languages that supports ngram, so don't
        // annoy them with an error dialog:
        System.err.println("Not loading ngram data, directory does not exist: " + ngramLangDir);
      }
    }
  }

  private void activateWord2VecModelRules(Language language) {
    if (config.getWord2VecDirectory() != null) {
      File word2vecDir = new File(config.getWord2VecDirectory(), language.getShortCode());
      if (word2vecDir.exists()) {
        try {
          languageTool.activateWord2VecModelRules(config.getWord2VecDirectory());
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "Error while loading word2vec model.\n" + e.getMessage());
        }
      } else {
        System.err.println("Not loading word2vec data, directory does not exist: " + word2vecDir);
      }
    }
  }

  private void init() {
    try {
      config = new Configuration(new File(System.getProperty("user.home")), CONFIG_FILE, null);
    } catch (IOException ex) {
      throw new RuntimeException("Could not load configuration", ex);
    }

    Language defaultLanguage = config.getLanguage();
    if (defaultLanguage == null) {
        defaultLanguage = Languages.getLanguageForLocale(Locale.getDefault());
    }

    /*
     * Warm-up: we have a lot of lazy init in LT, which causes the first check to
     * be very slow (several seconds) for languages with a lot of data and a lot of
     * rules. We just assume that the default language is the language that the user
     * often uses and init the LT object for that now, not just when it's first used.
     * This makes the first check feel much faster:
     */    
    reloadLanguageTool(defaultLanguage);

    checkExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.setName(t.getName() + "-lt-background");
        return t;
      }
    });

    check = new AtomicInteger(0);

    this.textComponent.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        mustDetectLanguage = config.getAutoDetect();
        recalculateSpans(e.getOffset(), e.getLength(), false);
        if (backgroundCheckEnabled) {
          checkDelayed(null);
        }
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        mustDetectLanguage = config.getAutoDetect();
        recalculateSpans(e.getOffset(), e.getLength(), true);
        if (backgroundCheckEnabled) {
          checkDelayed(null);
        }
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        mustDetectLanguage = config.getAutoDetect();
        if (backgroundCheckEnabled) {
          checkDelayed(null);
        }
      }
    });

    mouseListener = new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent me) {
      }

      @Override
      public void mousePressed(MouseEvent me) {
        if (me.isPopupTrigger()) {
          showPopup(me);
        }
      }

      @Override
      public void mouseReleased(MouseEvent me) {
        if (me.isPopupTrigger()) {
          showPopup(me);
        }
      }

      @Override
      public void mouseEntered(MouseEvent me) {}
      @Override
      public void mouseExited(MouseEvent me) {}
    };
    this.textComponent.addMouseListener(mouseListener);

    actionListener = e -> _actionPerformed(e);

    mustDetectLanguage = config.getAutoDetect();
    if (!this.textComponent.getText().isEmpty() && backgroundCheckEnabled) {
      checkImmediately(null);
    }
  }

  public int getMillisecondDelay() {
    return millisecondDelay;
  }

  /**
   * The text checking delay in milliseconds.
   */
  public void setMillisecondDelay(int millisecondDelay) {
    this.millisecondDelay = millisecondDelay;
  }

  public boolean isPopupMenuEnabled() {
    return popupMenuEnabled;
  }

  public void setPopupMenuEnabled(boolean popupMenuEnabled) {
    if (this.popupMenuEnabled == popupMenuEnabled) {
      return;
    }
    this.popupMenuEnabled = popupMenuEnabled;
    if (popupMenuEnabled) {
      textComponent.addMouseListener(mouseListener);
    } else {
      textComponent.removeMouseListener(mouseListener);
    }
  }

  public boolean isBackgroundCheckEnabled() {
    return backgroundCheckEnabled;
  }

  public void setBackgroundCheckEnabled(boolean backgroundCheckEnabled) {
    if (this.backgroundCheckEnabled == backgroundCheckEnabled) {
      return;
    }
    this.backgroundCheckEnabled = backgroundCheckEnabled;
    if (backgroundCheckEnabled) {
      checkImmediately(null);
    }
  }

  public void setLanguage(Language language) {
    reloadLanguageTool(language);
    if (backgroundCheckEnabled) {
      checkImmediately(null);
    }
  }

  Language getLanguage() {
      return this.languageTool.getLanguage();
  }

  public Configuration getConfig() {
    return config;
  }

  // called from Main.showOptions() and Main.tagTextAndDisplayResults()
  JLanguageTool getLanguageTool() {
    return languageTool;
  }

  void disableRule(String ruleId) {
    Rule rule = this.getRuleForId(ruleId);
    if (rule == null) {
      //System.err.println("No rule with id: <"+ruleId+">");
      return;
    }
    if (rule.isDefaultOff()) {
      config.getEnabledRuleIds().remove(ruleId);
    } else {
      config.getDisabledRuleIds().add(ruleId);
    }
    languageTool.disableRule(ruleId);
    updateHighlights(ruleId);
    fireEvent(LanguageToolEvent.Type.RULE_DISABLED, null);
  }

  void enableRule(String ruleId) {
    Rule rule = this.getRuleForId(ruleId);
    if (rule == null) {
      //System.err.println("No rule with id: <"+ruleId+">");
      return;
    }
    if (rule.isDefaultOff()) {
      config.getEnabledRuleIds().add(ruleId);
    } else {
      config.getDisabledRuleIds().remove(ruleId);
    }
    languageTool.enableRule(ruleId);
    fireEvent(LanguageToolEvent.Type.RULE_ENABLED, null);
    checkImmediately(null);
  }

  @Nullable
  private Span getSpan(int offset) {
    for (Span cur : documentSpans) {
      if (cur.end > cur.start && cur.start <= offset && offset < cur.end) {
        return cur;
      }
    }
    return null;
  }

  private void showPopup(MouseEvent event) {
    if (documentSpans.isEmpty() && languageTool.getDisabledRules().isEmpty()) {
      //No errors and no disabled Rules
      return;
    }

    int offset = this.textComponent.viewToModel(event.getPoint());
    Span span = getSpan(offset);
    JPopupMenu popup = new JPopupMenu("Grammar Menu");
    if (span != null) {
      JLabel msgItem = new JLabel("<html>"
              + span.msg.replace("<suggestion>", "<b>").replace("</suggestion>", "</b>")
              + "</html>");
      msgItem.setToolTipText(
              span.desc.replace("<suggestion>", "").replace("</suggestion>", ""));
      msgItem.setBorder(new JMenuItem().getBorder());
      popup.add(msgItem);

      popup.add(new JSeparator());

      for (String r : span.replacement) {
        ReplaceMenuItem item = new ReplaceMenuItem(r, span);
        popup.add(item);
        item.addActionListener(actionListener);
      }

      popup.add(new JSeparator());

      JMenuItem moreItem = new JMenuItem(messages.getString("guiMore"));
      moreItem.addActionListener(e -> showDialog(textComponent, span.msg, span.desc, span.rule, span.url));
      popup.add(moreItem);

      JMenuItem ignoreItem = new JMenuItem(messages.getString("guiTurnOffRule"));
      ignoreItem.addActionListener(e -> disableRule(span.rule.getId()));
      popup.add(ignoreItem);
      popup.applyComponentOrientation(
        ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    List<Rule> disabledRules = getDisabledRules();
    if (!disabledRules.isEmpty()) {
      JMenu activateRuleMenu = new JMenu(messages.getString("guiActivateRule"));
      addDisabledRulesToMenu(disabledRules, activateRuleMenu);
      popup.add(activateRuleMenu);
    }

    if (span != null) {
      textComponent.setCaretPosition(span.start);
      textComponent.moveCaretPosition(span.end);
    }

    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
        if (span != null) {
          textComponent.setCaretPosition(span.start);
        }
      }
    });
    popup.show(textComponent, event.getPoint().x, event.getPoint().y);

  }

  private List<Rule> getDisabledRules() {
    List<Rule> disabledRules = new ArrayList<>();
    for (String ruleId : languageTool.getDisabledRules()) {
      Rule rule = getRuleForId(ruleId);
      if (rule == null || rule.isDefaultOff()) {
        continue;
      }
      disabledRules.add(rule);
    }
    Collections.sort(disabledRules, (r1, r2) -> r1.getDescription().compareTo(r2.getDescription()));
    return disabledRules;
  }

  private void addDisabledRulesToMenu(List<Rule> disabledRules, JMenu menu) {
    if (disabledRules.size() <= MAX_RULES_NO_CATEGORY_MENU) {
      createRulesMenu(menu, disabledRules);
      return;
    }

    TreeMap<String, ArrayList<Rule>> categories = new TreeMap<>();
    for (Rule rule : disabledRules) {
      if (!categories.containsKey(rule.getCategory().getName())) {
        categories.put(rule.getCategory().getName(), new ArrayList<>());
      }
      categories.get(rule.getCategory().getName()).add(rule);
    }

    JMenu parent = menu;
    int count = 0;
    for (String category : categories.keySet()) {
      count++;
      JMenu submenu = new JMenu(category);
      parent.add(submenu);
      createRulesMenu(submenu, categories.get(category));

      if (categories.keySet().size() <= MAX_CATEGORIES_PER_MENU) {
        continue;
      }

      //if menu contains MAX_CATEGORIES_PER_MENU-1, add a `more` menu
      //but only if the remain entries are more than one
      if ((count % (MAX_CATEGORIES_PER_MENU - 1) == 0)
              && (categories.keySet().size() - count > 1)) {
        JMenu more = new JMenu(messages.getString("guiActivateRuleMoreCategories"));
        parent.add(more);
        parent = more;
      }
    }
  }

  private void createRulesMenu(JMenu parent, List<Rule> rules) {
    JMenu menu = parent;
    int count = 0;

    for (Rule rule : rules) {
      count++;
      String id = rule.getId();
      JMenuItem ruleItem = new JMenuItem(rule.getDescription());
      ruleItem.addActionListener(e -> enableRule(id));
      menu.add(ruleItem);

      if (rules.size() <= MAX_RULES_PER_MENU) {
        continue;
      }

      //if menu contains MAX_RULES_PER_MENU-1, add a `more` menu
      //but only if the remain entries are more than one
      if ((count % (MAX_RULES_PER_MENU - 1) == 0)
              && (rules.size() - count > 1)) {
        JMenu more = new JMenu(messages.getString("guiActivateRuleMoreRules"));
        menu.add(more);
        menu = more;
      }
    }
  }

  @Nullable
  Rule getRuleForId(String ruleId) {
    List<Rule> allRules = languageTool.getAllRules();
    for (Rule rule : allRules) {
      if (rule.getId().equals(ruleId)) {
        return rule;
      }
    }
    return null;
  }

  private void _actionPerformed(ActionEvent e) {
    ReplaceMenuItem src = (ReplaceMenuItem) e.getSource();

    this.documentSpans.remove(src.span);
    applySuggestion(e.getActionCommand(), src.span.start, src.span.end);
  }

  private void applySuggestion(String str, int start, int end) {
    if (end < start) {
      throw new IllegalArgumentException("end before start: " + end + " < " + start);
    }
    Document doc = this.textComponent.getDocument();
    if (doc != null) {
      try {
        if (this.undo != null) {
          this.undo.startCompoundEdit();
        }
        if (doc instanceof AbstractDocument) {
          ((AbstractDocument) doc).replace(start, end - start, str, null);
        } else {
          doc.remove(start, end - start);
          doc.insertString(start, str, null);
        }
      } catch (BadLocationException e) {
        throw new IllegalArgumentException(e);
      } finally {
        if (this.undo != null) {
          this.undo.endCompoundEdit();
        }
      }
    }
  }

  public void checkDelayed() {
    checkDelayed(null);
  }

  public void checkDelayed(Object caller) {
    check.getAndIncrement();
    checkExecutor.schedule(new RunnableImpl(caller), millisecondDelay, TimeUnit.MILLISECONDS);
  }

  public void checkImmediately() {
    checkImmediately(null);
  }

  public void checkImmediately(Object caller) {
    check.getAndIncrement();
    checkExecutor.schedule(new RunnableImpl(caller), 0, TimeUnit.MILLISECONDS);
  }

  Language autoDetectLanguage(String text) {
    Language lang = langIdentifier.detectLanguage(text);
    if (lang == null) {
      lang = Languages.getLanguageForLocale(Locale.getDefault());
    }
    if (lang.hasVariant()) {
      // UI only shows variants like "English (American)", not just "English", so use that:
      lang = lang.getDefaultLanguageVariant();
    }
    return lang;
  }

  private synchronized List<RuleMatch> checkText(Object caller) throws IOException {
    if (this.mustDetectLanguage) {
      mustDetectLanguage = false;
      if (!this.textComponent.getText().isEmpty()) {
        Language detectedLanguage = autoDetectLanguage(this.textComponent.getText());
        if (!detectedLanguage.equals(this.languageTool.getLanguage())) {
          reloadLanguageTool(detectedLanguage);
          if (SwingUtilities.isEventDispatchThread()) {
            fireEvent(LanguageToolEvent.Type.LANGUAGE_CHANGED, caller);
          } else {
            try {
              SwingUtilities.invokeAndWait(() -> fireEvent(LanguageToolEvent.Type.LANGUAGE_CHANGED, caller));
            } catch (InterruptedException ex) {
              //ignore
            } catch (InvocationTargetException ex) {
              throw new RuntimeException(ex);
            }
          }
        }
      }
    }
    if (SwingUtilities.isEventDispatchThread()) {
      fireEvent(LanguageToolEvent.Type.CHECKING_STARTED, caller);
    } else {
      try {
        SwingUtilities.invokeAndWait(() -> fireEvent(LanguageToolEvent.Type.CHECKING_STARTED, caller));
      } catch (InterruptedException ex) {
        //ignore
      } catch (InvocationTargetException ex) {
        throw new RuntimeException(ex);
      }
    }

    long startTime = System.currentTimeMillis();
    List<RuleMatch> matches = this.languageTool.check(this.textComponent.getText());
    long elapsedTime = System.currentTimeMillis() - startTime;

    int v = check.get();
    if (v == 0) {
      if (!SwingUtilities.isEventDispatchThread()) {
        SwingUtilities.invokeLater(() -> {
          updateHighlights(matches);
          fireEvent(LanguageToolEvent.Type.CHECKING_FINISHED, caller, elapsedTime);
        });
      } else {
        updateHighlights(matches);
        fireEvent(LanguageToolEvent.Type.CHECKING_FINISHED, caller, elapsedTime);
      }
    }
    return matches;
  }

  private void removeHighlights() {
    for (Highlighter.Highlight hl : textComponent.getHighlighter().getHighlights()) {
      if (hl.getPainter() instanceof HighlightPainter) {
        textComponent.getHighlighter().removeHighlight(hl);
      }
    }
  }

  private void recalculateSpans(int offset, int length, boolean remove) {
    if (length == 0) {
      return;
    }
    for (Span span : this.documentSpans) {
      if (offset >= span.end) {
        continue;
      }
      if (!remove) {
        if (offset <= span.start) {
          span.start += length;
        }
        span.end += length;
      } else {
        if (offset + length <= span.end) {
          if (offset > span.start) {
            //
          } else if (offset + length <= span.start) {
            span.start -= length;
          } else {
            span.start = offset;
          }
          span.end -= length;
        } else {
          span.end -= Math.min(length, span.end - offset);
        }
      }
    }
    updateHighlights();
  }

  private void updateHighlights(String disabledRule) {
    List<Span> spans = new ArrayList<>();
    List<RuleMatch> matches = new ArrayList<>();
    for (RuleMatch match : ruleMatches) {
      if (match.getRule().getId().equals(disabledRule)) {
        continue;
      }
      matches.add(match);
      spans.add(new Span(match));
    }
    prepareUpdateHighlights(matches, spans);
  }

  private void updateHighlights(List<RuleMatch> matches) {
    List<Span> spans = new ArrayList<>();
    for (RuleMatch match : matches) {
      spans.add(new Span(match));
    }
    prepareUpdateHighlights(matches, spans);
  }

  private void prepareUpdateHighlights(List<RuleMatch> matches, List<Span> spans) {
    ruleMatches.clear();
    documentSpans.clear();
    ruleMatches.addAll(matches);
    documentSpans.addAll(spans);
    updateHighlights();
  }

  private void updateHighlights() {
    removeHighlights();

    Highlighter h = textComponent.getHighlighter();

    for (Span span : documentSpans) {
      if (span.start == span.end) {
        continue;
      }
      try {
        if (span.start < span.end) { //to avoid the BadLocationException
          ITSIssueType issueType = span.rule.getLocQualityIssueType();
          Color ulColor = config.getUnderlineColor(span.rule.getCategory().getName(), span.rule.getId());
          Color colorForIssueType = getConfig().getErrorColors().get(issueType);
          Color underlineColor = ITSIssueType.Misspelling == span.rule.getLocQualityIssueType() ? Color.red : ulColor;
          HighlightPainter painter = new HighlightPainter(colorForIssueType, underlineColor);
          h.addHighlight(span.start, span.end, painter);
        }
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
    }
  }

  private void showDialog(Component parent, String title, String message, Rule rule, URL url) {
    Tools.showRuleInfoDialog(parent, title, message, rule, url, messages, languageTool.getLanguage().getShortCodeWithCountryAndVariant());
  }

  private static class ReplaceMenuItem extends JMenuItem {

    private final Span span;

    private ReplaceMenuItem(String name, Span span) {
      super(name);
      this.span = span;
    }
  }

  private static class Span {

    private static final int MAX_SUGGESTIONS = 5;
    
    private int start;
    private int end;
    private final String msg;
    private final String desc;
    private final List<String> replacement;
    private final Rule rule;
    private final URL url;

    private Span(RuleMatch match) {
      start = match.getFromPos();
      end = match.getToPos();
      String tmp = match.getShortMessage();
      if (StringUtils.isEmpty(tmp)) {
        tmp = match.getMessage();
      }
      msg = Tools.shortenComment(tmp);
      desc = match.getMessage();
      replacement = new ArrayList<>();
      List<String> repl = match.getSuggestedReplacements();
      replacement.addAll(repl.subList(0, Math.min(MAX_SUGGESTIONS, repl.size())));
      rule = match.getRule();
      url = match.getUrl() != null ? match.getUrl() : rule.getUrl();
    }
  }

  private class RunnableImpl implements Runnable {

    private final Object caller;

    private RunnableImpl(Object caller) {
      this.caller = caller;
    }

    @Override
    public void run() {
      int v = check.decrementAndGet();
      if (v != 0) {
        return;
      }
      try {
        checkText(caller);
      } catch (Exception ex) {
        Tools.showError(ex);
      }
    }
  }
}
