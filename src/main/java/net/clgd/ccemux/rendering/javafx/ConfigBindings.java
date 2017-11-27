package net.clgd.ccemux.rendering.javafx;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.BiConsumer;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.*;
import lombok.experimental.UtilityClass;
import net.clgd.ccemux.config.ConfigProperty;

@UtilityClass
public class ConfigBindings {
	/**
	 * A JavaFX {@link Property} implementation that wraps a
	 * {@link ConfigProperty}
	 * 
	 * @author apemanzilla
	 *
	 * @param <T>
	 */
	@RequiredArgsConstructor
	public static class WrappedConfigProperty<T> extends ObjectProperty<T> {
		@Value
		public class WrappedChangeListener implements BiConsumer<T, T> {
			private final ChangeListener<? super T> changeListener;

			@Override
			public void accept(T from, T to) {
				changeListener.changed(WrappedConfigProperty.this, from, to);
			}
		}

		@Value
		public class WrappedInvalidationListener implements BiConsumer<T, T> {
			private final InvalidationListener invalidationListener;

			@Override
			public void accept(T arg0, T arg1) {
				invalidationListener.invalidated(WrappedConfigProperty.this);
			}
		}

		@Getter
		private final ConfigProperty<T> cfgProp;

		@Override
		public Object getBean() {
			return null;
		}

		@Override
		public String getName() {
			return cfgProp.getKey();
		}

		@Override
		public void set(T value) {
			cfgProp.set(value);
		}

		@Override
		public T get() {
			return Optional.ofNullable(cfgProp.get()).orElse(cfgProp.getDefaultValue());
		}

		private WeakReference<ObservableValue<? extends T>> bindTarget;

		private WeakReference<ChangeListener<T>> bindListener;

		@Override
		public void bind(ObservableValue<? extends T> to) {
			bindTarget = new WeakReference<>(to);

			ChangeListener<T> l = (s, o, n) -> cfgProp.set(n);
			to.addListener(l);
			bindListener = new WeakReference<>(l);
		}

		@Override
		public boolean isBound() {
			return bindTarget == null || bindTarget.get() == null;
		}

		@Override
		public void unbind() {
			if (bindTarget != null) {
				if (bindListener.get() != null && bindTarget.get() != null) {
					bindTarget.get().removeListener(bindListener.get());
				}

				bindTarget.clear();
				bindListener.clear();
			}
		}

		@Override
		public void addListener(ChangeListener<? super T> l) {
			cfgProp.addListener(new WrappedChangeListener(l));
		}

		@Override
		public void removeListener(ChangeListener<? super T> l) {
			cfgProp.removeListener(new WrappedChangeListener(l));

		}

		@Override
		public void addListener(InvalidationListener l) {
			cfgProp.addListener(new WrappedInvalidationListener(l));
		}

		@Override
		public void removeListener(InvalidationListener l) {
			cfgProp.removeListener(new WrappedInvalidationListener(l));
		}
	}

	/**
	 * Wraps a given {@link ConfigProperty} as a JavaFX {@link Property}
	 * 
	 * @param cfgProp
	 * @return
	 */
	public static <T> Property<T> wrap(ConfigProperty<T> cfgProp) {
		return new WrappedConfigProperty<>(cfgProp);
	}
}
