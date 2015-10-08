(ns minimal-chat.core
  (:require [cljs.core.async :refer [<!]]
            [clojure.string :as str]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [re-frame.core :as rf :refer [dispatch
                                          dispatch-sync
                                          register-handler
                                          register-sub
                                          subscribe]]
            [matchbox.async :as ma]
            [matchbox.core :as m])
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go-loop]])
  (:import goog.History))

;; -- REPLACE with your own DB location ---------
(def firebase-io-root "https://shining-torch-2145.firebaseIO.com")
;; ----------------------------------------------

;; Connection to Firebase
(defonce fb-root (m/connect firebase-io-root))

(def initial-state
  {:chat-room nil
   :messages []
   :username (str "Guest" (rand-int 100))})

(defonce fb-messages (atom nil))
(defonce messages (reagent/atom []))

;; -- Firebase helpers ------

(defn bind-to-ratom [ratom ref & [korks]]
  (let [ch (ma/listen-to< ref korks :value)]
    (go-loop [[key val] (<! ch)]
      (reset! ratom val)
      (recur (<! ch)))))

(defn firebase-swap! [fb-ref f & args]
  (apply m/swap! fb-ref f args))

;; -- Helpers ---------------

(defn- random-four-characters []
  (->> (repeatedly #(rand-nth ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z"]))
       (take 4)
       (str/join)
       (str/upper-case)))

(defn join-room [id]
  (let [fb-item (m/get-in fb-root [(str/lower-case id) :messages])]
    (reset! fb-messages fb-item)
    (bind-to-ratom messages fb-item)))

;; -- Subscriptions ---------

(register-sub
  :username-input
  (fn [db _]
    (reaction (:username @db))))

(register-sub
  :current-chat-room
  (fn [db _]
    (reaction (:chat-room @db))))

(register-sub
  :chat-room-messages
  (fn [db _]
    (reaction (:messages @db))))

;; -- Event Handlers --------

(register-handler
  :initialize
  (fn [db _]
    (merge db initial-state)))

(register-handler
  :set-username-input
  (fn [db [_ value]]
    (assoc db :username value)))

(register-handler
  :join-chat-room
  (fn [db [_ room-id]]
    (join-room room-id)
    (assoc db :chat-room room-id)))

(register-handler
  :send-message
  (fn [db [_ username message chat-room]]
    (m/conj-in! fb-root [(-> chat-room (str/lower-case) (keyword))
                         :messages]
                [username message])
    db))

;; -- View Components -------

(defn username-input []
  (let [username (subscribe [:username-input])]
    [:input {:type "text"
             :value @username
             :on-change #(dispatch [:set-username-input (-> % .-target .-value)])}]))

(defn message-form []
  (let [message   (reagent/atom "")
        username  (subscribe [:username-input])
        chat-room (subscribe [:current-chat-room])]
    [:form {:on-submit (fn [e]
                         ;; prevent redirect
                         (.preventDefault e)
                         (dispatch-sync [:send-message @username @message @chat-room])
                         ;; reset the form
                         (.reset (.-target e)))}
     [:input {:type "text"
              :on-change #(reset! message (-> % .-target .-value))}]
     [:button {:type "submit"} "Send"]]))

;; -- Views -----------------

(defn on-going-chat []
  [:div
   [:label "Your username: "]
   [username-input]

   [:div#messages
    (for [[message index] (map vector (take-last 30 @messages) (range))]
      ^{:key (str "message-" index)}
      [:div [:span.username (first message) ": "] [:span.message (second message)]])]

   [message-form]])

(defn join-room-view []
  (let [this-room        (atom (random-four-characters))
        change-location #(set! (.-location js/window) (str "#/room/" @this-room))]
    [:div
     [:input {:placeholder "Room Name"
              :type "text"
              :on-change #(reset! this-room (-> % .-target .-value))}]
     [:button {:type "submit"
               :on-click change-location}
      "Join Room"]
     [:button {:on-click change-location
               :type "submit"}
      "Random Room"]]))

(defn home-page []
  (let [chat-room (subscribe [:current-chat-room])
        messages  (subscribe [:chat-room-messages])]
    [:div
     [:div.container
      [:div.page-header
       [:h2 "Minimal Chat"]]

      (if @chat-room
        [on-going-chat]
        [join-room-view])

      [:div [:br] [:br] [:br]
       "STATE!!!!"
       [:div "Messages: " (pr-str @messages)]]]]))

(defn current-page []
  [:div [(session/get :current-page)]])

;; -- Routes and History ----

(secretary/defroute "/" []
  (dispatch [:exit-chat-room])
  (session/put! :current-page #'home-page))

(secretary/defroute "/room/:id" [id]
  (dispatch [:join-chat-room id])
  (session/put! :current-page #'home-page))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn init! []
  (hook-browser-navigation!)
  (secretary/set-config! :prefix "#")
  (dispatch-sync [:initialize])

  ;; mount root
  (reagent/render-component [current-page] (.getElementById js/document "app")))

